import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Fish, IndianRupee, Pencil, Scale, Waves, XCircle } from 'lucide-react';
import { toast } from 'sonner';
import {
  useCancelHarvest,
  useHarvestAnalytics,
  useHistoryHarvests,
  usePondsBySite,
  useUpdateHarvest,
} from '@/api/queries';
import type { HarvestHistoryResponse } from '@/api/types';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import { SiteSelector } from '@/components/common/SiteSelector';
import { StatCard } from '@/components/common/StatCard';
import { EmptyBlock, LoadingBlock } from '@/components/common/StateViews';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatCurrency, formatDate, formatKg } from '@/lib/format';

const editSchema = z.object({
  harvestQuantityKg: z.coerce.number().positive('Must be greater than 0'),
  buyerName: z.string().optional(),
  sellingPricePerKg: z.coerce.number().min(0).optional(),
  vehicleNumber: z.string().optional(),
  remarks: z.string().optional(),
});
type EditValues = z.infer<typeof editSchema>;

function EditHarvestDialog({
  harvest,
  pondId,
}: {
  harvest: HarvestHistoryResponse;
  pondId: string;
}) {
  const [open, setOpen] = useState(false);
  const update = useUpdateHarvest(pondId);
  const form = useForm<EditValues>({
    resolver: zodResolver(editSchema),
    values: {
      harvestQuantityKg: harvest.harvestQuantityKg,
      buyerName: harvest.buyerName ?? '',
      sellingPricePerKg: harvest.sellingPricePerKg ?? 0,
      vehicleNumber: '',
      remarks: '',
    },
  });

  const onSubmit = async (v: EditValues) => {
    try {
      await update.mutateAsync({
        id: harvest.harvestId,
        body: {
          harvestQuantityKg: v.harvestQuantityKg,
          buyerName: v.buyerName || undefined,
          sellingPricePerKg: v.sellingPricePerKg || undefined,
          vehicleNumber: v.vehicleNumber || undefined,
          remarks: v.remarks || undefined,
        },
      });
      toast.success('Harvest updated');
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not update harvest');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="icon" aria-label="Edit harvest">
          <Pencil className="h-4 w-4" />
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit harvest</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="harvestQuantityKg"
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
            <FormField
              control={form.control}
              name="buyerName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Buyer</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="sellingPricePerKg"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Price per kg</FormLabel>
                  <FormControl>
                    <Input type="number" step="0.01" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="vehicleNumber"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Vehicle number</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="remarks"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Remarks</FormLabel>
                  <FormControl>
                    <Textarea rows={2} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={update.isPending}>
              {update.isPending ? 'Saving…' : 'Save changes'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

function CancelHarvestButton({
  harvestId,
  pondId,
}: {
  harvestId: string;
  pondId: string;
}) {
  const cancel = useCancelHarvest(pondId);
  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant="ghost" size="icon" aria-label="Cancel harvest">
          <XCircle className="h-4 w-4 text-destructive" />
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Cancel this harvest?</AlertDialogTitle>
          <AlertDialogDescription>
            The harvest is voided and its cycle is reopened. This can't be undone
            from here.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Keep</AlertDialogCancel>
          <AlertDialogAction
            onClick={() =>
              cancel.mutate(
                { id: harvestId, reason: 'Cancelled by admin' },
                {
                  onSuccess: () => toast.success('Harvest cancelled'),
                  onError: (e) =>
                    toast.error(e instanceof Error ? e.message : 'Failed'),
                },
              )
            }
          >
            Cancel harvest
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

export function HarvestedPage() {
  const { sites, siteId, select } = useSelectedSite();
  const ponds = usePondsBySite(siteId);
  const [pondId, setPondId] = useState<string | null>(null);
  const summary = useHarvestAnalytics(pondId);
  const harvests = useHistoryHarvests(pondId);

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      <Select value={pondId ?? undefined} onValueChange={setPondId}>
        <SelectTrigger>
          <SelectValue placeholder="Select a pond" />
        </SelectTrigger>
        <SelectContent>
          {(ponds.data ?? []).map((p) => (
            <SelectItem key={p.id} value={p.id}>
              {p.pondName} ({p.pondCode})
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {!pondId && (
        <EmptyBlock
          icon={<Waves className="h-6 w-6" />}
          title="Select a pond"
          description="Pick a pond to see its harvest totals and records."
        />
      )}

      {pondId && summary.data && (
        <div className="grid grid-cols-3 gap-2">
          <StatCard
            label="Harvests"
            value={String(summary.data.harvestCount)}
            icon={Fish}
          />
          <StatCard
            label="Total"
            value={formatKg(summary.data.totalHarvestKg)}
            icon={Scale}
          />
          <StatCard
            label="Revenue"
            value={formatCurrency(summary.data.totalRevenue)}
            icon={IndianRupee}
            tone="success"
          />
        </div>
      )}

      {pondId && (
        <div className="space-y-2">
          {harvests.isLoading && <LoadingBlock label="Loading harvests…" />}
          {harvests.data?.length === 0 && (
            <EmptyBlock
              icon={<Fish className="h-6 w-6" />}
              title="No harvests"
              description="This pond has no harvest records yet."
            />
          )}
          {harvests.data?.map((h) => (
            <Card key={h.harvestId}>
              <CardContent className="flex items-start gap-3 p-4">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-emerald-500/15 text-emerald-600">
                  <Fish className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="font-medium">
                    {formatKg(h.harvestQuantityKg)}
                    {h.sellingPricePerKg != null &&
                      ` @ ${formatCurrency(h.sellingPricePerKg)}/kg`}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Cycle #{h.cycleNumber} · {formatDate(h.harvestDate)}
                    {h.buyerName ? ` · ${h.buyerName}` : ''}
                  </p>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <p className="font-semibold">{formatCurrency(h.totalAmount)}</p>
                  <Badge
                    variant={h.status === 'ACTIVE' ? 'default' : 'secondary'}
                  >
                    {h.status}
                  </Badge>
                  {h.status === 'ACTIVE' && pondId && (
                    <div className="flex items-center">
                      <EditHarvestDialog harvest={h} pondId={pondId} />
                      <CancelHarvestButton harvestId={h.harvestId} pondId={pondId} />
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
