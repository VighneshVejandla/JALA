import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Play, Sprout } from 'lucide-react';
import { toast } from 'sonner';
import { useCreateCycle, useHarvestCycle } from '@/api/queries';
import type { PondCycleResponse, ShrimpSpecies } from '@/api/types';
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
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
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
import { formatDate, formatNumber } from '@/lib/format';

const SPECIES: ShrimpSpecies[] = ['VANNAMEI', 'TIGER'];

const schema = z.object({
  species: z.enum(['VANNAMEI', 'TIGER']),
  stockingDate: z.string().min(1, 'Required'),
  shrimpCount: z.coerce.number().int().positive('Must be greater than 0'),
});
type FormValues = z.infer<typeof schema>;

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function StartCycleDialog({ pondId }: { pondId: string }) {
  const [open, setOpen] = useState(false);
  const createCycle = useCreateCycle();
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      species: 'VANNAMEI',
      stockingDate: todayIso(),
      shrimpCount: 0,
    },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await createCycle.mutateAsync({ pondId, ...v });
      toast.success('Cycle started');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not start cycle');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Play className="mr-1 h-4 w-4" /> Start cycle
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Start new cycle</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="species"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Species</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {SPECIES.map((s) => (
                        <SelectItem key={s} value={s}>
                          {s}
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
              name="stockingDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Stocking date</FormLabel>
                  <FormControl>
                    <Input type="date" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="shrimpCount"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Shrimp count</FormLabel>
                  <FormControl>
                    <Input type="number" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button
              type="submit"
              className="w-full"
              disabled={createCycle.isPending}
            >
              {createCycle.isPending ? 'Starting…' : 'Start cycle'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export function CycleSection({
  pondId,
  cycle,
  isLoading,
}: {
  pondId: string;
  cycle: PondCycleResponse | null;
  isLoading: boolean;
}) {
  const harvestCycle = useHarvestCycle(pondId);
  const isActive = !!cycle && cycle.status === 'ACTIVE';

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-base">Current cycle</CardTitle>
        {!isLoading && !isActive && <StartCycleDialog pondId={pondId} />}
      </CardHeader>
      <CardContent className="space-y-3">
        {isLoading && <p className="text-sm text-muted-foreground">Loading…</p>}

        {!isLoading && !isActive && (
          <p className="text-sm text-muted-foreground">
            No active cycle. Start a cycle to record feed, medicine and harvest.
          </p>
        )}

        {isActive && cycle && (
          <>
            <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
              <Detail label="Cycle #" value={formatNumber(cycle.cycleNumber)} />
              <Detail label="Species" value={cycle.species} />
              <Detail label="Stocked" value={formatDate(cycle.stockingDate)} />
              <Detail label="Shrimp count" value={formatNumber(cycle.shrimpCount)} />
              <div className="col-span-2">
                <Badge variant="secondary">{cycle.status}</Badge>
              </div>
            </div>

            <AlertDialog>
              <AlertDialogTrigger asChild>
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full"
                  disabled={harvestCycle.isPending}
                >
                  <Sprout className="mr-1 h-4 w-4" /> Convert to harvested
                </Button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Close this cycle?</AlertDialogTitle>
                  <AlertDialogDescription>
                    This marks cycle #{cycle.cycleNumber} as harvested and moves
                    it to history. You can then start a new cycle for this pond.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>Cancel</AlertDialogCancel>
                  <AlertDialogAction
                    onClick={() =>
                      harvestCycle.mutate(cycle.id, {
                        onSuccess: () => toast.success('Cycle harvested'),
                        onError: (e) =>
                          toast.error(
                            e instanceof Error ? e.message : 'Failed',
                          ),
                      })
                    }
                  >
                    Confirm
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="space-y-0.5">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className="font-medium text-foreground">{value}</p>
    </div>
  );
}
