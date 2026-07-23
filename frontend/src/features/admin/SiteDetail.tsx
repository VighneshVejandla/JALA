import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  ArrowLeft,
  ChevronRight,
  PackageOpen,
  Plus,
  Waves,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  useCreatePond,
  useInventory,
  usePondsBySite,
  useSetPondActive,
  useSetSiteActive,
  useSite,
} from '@/api/queries';
import { ROUTES } from '@/constants/routes';
import { Switch } from '@/components/ui/switch';
import { StatCard } from '@/components/common/StatCard';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
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
import { formatKg, formatNumber } from '@/lib/format';

const schema = z.object({
  pondCode: z.string().min(1, 'Required'),
  pondName: z.string().min(1, 'Required'),
  pondAcres: z.coerce.number().positive('Must be greater than 0'),
});
type FormValues = z.infer<typeof schema>;

function AddPondDialog({ siteId }: { siteId: string }) {
  const [open, setOpen] = useState(false);
  const createPond = useCreatePond();
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { pondCode: '', pondName: '', pondAcres: 0 },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await createPond.mutateAsync({ siteId, ...v });
      toast.success('Pond created');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not create pond');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Add pond
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add pond</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="pondCode"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Pond code</FormLabel>
                  <FormControl>
                    <Input placeholder="P-01" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="pondName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Pond name</FormLabel>
                  <FormControl>
                    <Input placeholder="Pond One" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="pondAcres"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Pond acres</FormLabel>
                  <FormControl>
                    <Input type="number" step="0.01" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button
              type="submit"
              className="w-full"
              disabled={createPond.isPending}
            >
              {createPond.isPending ? 'Creating…' : 'Create pond'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export function SiteDetail() {
  const { siteId } = useParams<{ siteId: string }>();
  const navigate = useNavigate();
  const site = useSite(siteId ?? null);
  const inventory = useInventory(siteId ?? null);
  const ponds = usePondsBySite(siteId ?? null);
  const setSiteActive = useSetSiteActive();
  const setPondActive = useSetPondActive(siteId ?? '');

  return (
    <div className="space-y-4">
      <Button
        variant="ghost"
        size="sm"
        className="-ml-2"
        onClick={() => navigate(ROUTES.adminSites)}
      >
        <ArrowLeft className="mr-1 h-4 w-4" /> Sites
      </Button>

      {site.isLoading && <LoadingBlock label="Loading site…" />}
      {site.isError && (
        <ErrorBlock
          message="Could not load this site."
          onRetry={() => site.refetch()}
        />
      )}

      {site.data && (
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-semibold">{site.data.siteName}</h2>
            <Badge variant={site.data.isActive ? 'default' : 'secondary'}>
              {site.data.isActive ? 'Active' : 'Inactive'}
            </Badge>
            <Switch
              className="ml-auto"
              checked={site.data.isActive}
              disabled={setSiteActive.isPending}
              aria-label={site.data.isActive ? 'Deactivate site' : 'Activate site'}
              onCheckedChange={(next) =>
                setSiteActive.mutate(
                  { id: site.data!.id, active: next },
                  {
                    onError: (e) =>
                      toast.error(e instanceof Error ? e.message : 'Update failed'),
                  },
                )
              }
            />
          </div>
          <p className="text-sm text-muted-foreground">
            {site.data.siteCode}
            {site.data.location && ` · ${site.data.location}`}
            {site.data.totalAcres != null &&
              ` · ${formatNumber(site.data.totalAcres)} acres`}
          </p>
        </div>
      )}

      {inventory.data && (
        <div className="grid grid-cols-2 gap-3">
          <StatCard
            label="Available Feed"
            value={formatKg(inventory.data.availableKg)}
            icon={PackageOpen}
            hint={`${formatNumber(inventory.data.availableBags)} bags`}
          />
          <StatCard
            label="Consumed (month)"
            value={formatKg(inventory.data.consumedMonthKg)}
            icon={PackageOpen}
          />
        </div>
      )}

      <div className="flex items-center justify-between pt-2">
        <h3 className="font-semibold">Ponds</h3>
        {siteId && <AddPondDialog siteId={siteId} />}
      </div>

      {ponds.isLoading && <LoadingBlock label="Loading ponds…" />}
      {ponds.data && ponds.data.length === 0 && (
        <EmptyBlock
          icon={<Waves className="h-6 w-6" />}
          title="No ponds"
          description="Add a pond to start a cycle."
        />
      )}

      <div className="space-y-3">
        {ponds.data?.map((pond) => (
          <Card
            key={pond.id}
            role="button"
            tabIndex={0}
            onClick={() => navigate(ROUTES.adminPondManage(pond.id))}
            onKeyDown={(e) => {
              if (e.key === 'Enter') navigate(ROUTES.adminPondManage(pond.id));
            }}
            className="cursor-pointer transition-colors hover:border-primary/40"
          >
            <CardContent className="flex items-center gap-3 p-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <Waves className="h-5 w-5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{pond.pondName}</p>
                <p className="truncate text-xs text-muted-foreground">
                  {pond.pondCode}
                  {pond.pondAcres != null && ` · ${pond.pondAcres} acres`}
                </p>
              </div>
              <Switch
                checked={pond.isActive}
                disabled={setPondActive.isPending}
                aria-label={
                  pond.isActive ? `Deactivate ${pond.pondName}` : `Activate ${pond.pondName}`
                }
                onClick={(e) => e.stopPropagation()}
                onCheckedChange={(next) =>
                  setPondActive.mutate(
                    { id: pond.id, active: next },
                    {
                      onError: (e) =>
                        toast.error(e instanceof Error ? e.message : 'Update failed'),
                    },
                  )
                }
              />
              <ChevronRight className="h-5 w-5 text-muted-foreground" />
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
