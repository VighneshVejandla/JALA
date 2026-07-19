import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Clock, Plus, Trash2, Utensils } from 'lucide-react';
import { toast } from 'sonner';
import {
  useCreateFeedEntry,
  useCreateFeedSchedule,
  useFeedAnalytics,
  useFeedEntries,
  useFeedSchedules,
} from '@/api/queries';
import { FEED_SIZE_LABELS, type FeedSize } from '@/api/types';
import { StatCard } from '@/components/common/StatCard';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatKg, formatNumber } from '@/lib/format';

const FEED_SIZES = Object.keys(FEED_SIZE_LABELS) as FeedSize[];

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

// --- Add feeding sessions -------------------------------------------------

function AddSessionsDialog({ cycleId }: { cycleId: string }) {
  const [open, setOpen] = useState(false);
  const [times, setTimes] = useState<string[]>(['08:00']);
  const createSchedule = useCreateFeedSchedule();

  const submit = async () => {
    const feedingTimes = times.map((t) => t.trim()).filter(Boolean);
    if (feedingTimes.length === 0) {
      toast.error('Add at least one feeding time');
      return;
    }
    try {
      await createSchedule.mutateAsync({ pondCycleId: cycleId, feedingTimes });
      toast.success('Sessions added');
      setTimes(['08:00']);
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not add sessions');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="outline">
          <Plus className="mr-1 h-4 w-4" /> Sessions
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add feeding sessions</DialogTitle>
        </DialogHeader>
        <div className="space-y-2">
          {times.map((t, i) => (
            <div key={i} className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <Input
                type="time"
                value={t}
                aria-label={`Feeding time ${i + 1}`}
                onChange={(e) =>
                  setTimes((prev) =>
                    prev.map((v, idx) => (idx === i ? e.target.value : v)),
                  )
                }
              />
              {times.length > 1 && (
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  aria-label={`Remove time ${i + 1}`}
                  onClick={() =>
                    setTimes((prev) => prev.filter((_, idx) => idx !== i))
                  }
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              )}
            </div>
          ))}
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => setTimes((prev) => [...prev, '12:00'])}
          >
            <Plus className="mr-1 h-4 w-4" /> Add time
          </Button>
        </div>
        <Button onClick={submit} disabled={createSchedule.isPending} className="w-full">
          {createSchedule.isPending ? 'Adding…' : 'Add sessions'}
        </Button>
      </DialogContent>
    </Dialog>
  );
}

// --- Record a feed amount for a session -----------------------------------

const entrySchema = z.object({
  feedScheduleId: z.string().min(1, 'Select a session'),
  feedSize: z.enum([
    'ONE',
    'TWO',
    'TWO_S',
    'THREE',
    'THREE_S',
    'FOUR',
    'FOUR_S',
    'FIVE',
  ]),
  feedQuantityKg: z.coerce.number().positive('Must be greater than 0'),
  remarks: z.string().optional(),
});
type EntryValues = z.infer<typeof entrySchema>;

function RecordFeedDialog({
  cycleId,
  sessions,
  date,
}: {
  cycleId: string;
  sessions: { id: string; sessionNumber: number; feedingTime: string }[];
  date: string;
}) {
  const [open, setOpen] = useState(false);
  const createEntry = useCreateFeedEntry(date);
  const form = useForm<EntryValues>({
    resolver: zodResolver(entrySchema),
    defaultValues: { feedScheduleId: '', feedSize: 'ONE', feedQuantityKg: 0 },
  });

  const onSubmit = async (v: EntryValues) => {
    try {
      await createEntry.mutateAsync({
        pondCycleId: cycleId,
        feedScheduleId: v.feedScheduleId,
        feedDate: date,
        feedSize: v.feedSize,
        feedQuantityKg: v.feedQuantityKg,
        remarks: v.remarks || undefined,
      });
      toast.success('Feed recorded');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not record feed');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" disabled={sessions.length === 0}>
          <Plus className="mr-1 h-4 w-4" /> Record feed
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Record feed</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="feedScheduleId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Session</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a session" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {sessions.map((s) => (
                        <SelectItem key={s.id} value={s.id}>
                          Session {s.sessionNumber} · {s.feedingTime.slice(0, 5)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="feedSize"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Feed size</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {FEED_SIZES.map((s) => (
                        <SelectItem key={s} value={s}>
                          {FEED_SIZE_LABELS[s]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="feedQuantityKg"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Quantity (kg)</FormLabel>
                  <FormControl>
                    <Input type="number" step="0.01" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={createEntry.isPending}>
              {createEntry.isPending ? 'Saving…' : 'Save feed entry'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

// --- Section --------------------------------------------------------------

export function FeedSection({
  cycleId,
  pondId,
  canManageSessions = true,
}: {
  cycleId: string;
  pondId: string;
  /** Only admins can create feeding sessions; workers record against them. */
  canManageSessions?: boolean;
}) {
  const date = todayIso();
  const analytics = useFeedAnalytics(pondId);
  const schedules = useFeedSchedules(cycleId);
  const entries = useFeedEntries(cycleId, date);
  const sessions = schedules.data ?? [];

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-base">Feed</CardTitle>
        <div className="flex gap-2">
          {canManageSessions && <AddSessionsDialog cycleId={cycleId} />}
          <RecordFeedDialog cycleId={cycleId} sessions={sessions} date={date} />
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {analytics.data && (
          <div className="grid grid-cols-3 gap-2">
            <StatCard
              label="Today"
              value={formatKg(analytics.data.todayFeedKg)}
              icon={Utensils}
            />
            <StatCard
              label="Week"
              value={formatKg(analytics.data.weekFeedKg)}
              icon={Utensils}
            />
            <StatCard
              label="Month"
              value={formatKg(analytics.data.monthFeedKg)}
              icon={Utensils}
            />
          </div>
        )}

        <div>
          <p className="mb-2 text-xs font-medium text-muted-foreground">
            Sessions ({formatNumber(sessions.length)})
          </p>
          {sessions.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              No feeding sessions yet — add sessions to record feed.
            </p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {sessions.map((s) => (
                <Badge key={s.id} variant="secondary">
                  #{s.sessionNumber} · {s.feedingTime.slice(0, 5)}
                </Badge>
              ))}
            </div>
          )}
        </div>

        <div>
          <p className="mb-2 text-xs font-medium text-muted-foreground">
            Today's entries
          </p>
          {entries.data && entries.data.length === 0 && (
            <p className="text-sm text-muted-foreground">No feed recorded today.</p>
          )}
          <div className="space-y-2">
            {entries.data?.map((e) => (
              <div
                key={e.id}
                className="flex items-center justify-between rounded-lg border border-border p-2 text-sm"
              >
                <span>
                  Session {e.sessionNumber} · size {FEED_SIZE_LABELS[e.feedSize]}
                </span>
                <span className="font-medium">{formatKg(e.feedQuantityKg)}</span>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
