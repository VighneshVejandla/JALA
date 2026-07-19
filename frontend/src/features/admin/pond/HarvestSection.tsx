import { useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Fish, Plus, Receipt } from 'lucide-react';
import { toast } from 'sonner';
import { useCreateHarvest, useHarvests } from '@/api/queries';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
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
import { formatCurrency, formatDate, formatKg } from '@/lib/format';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

const schema = z.object({
  harvestDate: z.string().min(1, 'Required'),
  harvestQuantityKg: z.coerce.number().positive('Must be greater than 0'),
  buyerName: z.string().optional(),
  sellingPricePerKg: z.coerce.number().min(0).optional(),
  vehicleNumber: z.string().optional(),
  remarks: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

function RecordHarvestDialog({ cycleId }: { cycleId: string }) {
  const [open, setOpen] = useState(false);
  const [bill, setBill] = useState<File | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);
  const createHarvest = useCreateHarvest(cycleId);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      harvestDate: todayIso(),
      harvestQuantityKg: 0,
      buyerName: '',
      sellingPricePerKg: 0,
      vehicleNumber: '',
      remarks: '',
    },
  });

  const onSubmit = async (v: FormValues) => {
    if (!bill) {
      toast.error('A bill photo is required');
      return;
    }
    const fd = new FormData();
    fd.append('pondCycleId', cycleId);
    fd.append('harvestDate', v.harvestDate);
    fd.append('harvestQuantityKg', String(v.harvestQuantityKg));
    fd.append('billPhoto', bill);
    if (v.buyerName) fd.append('buyerName', v.buyerName);
    if (v.sellingPricePerKg)
      fd.append('sellingPricePerKg', String(v.sellingPricePerKg));
    if (v.vehicleNumber) fd.append('vehicleNumber', v.vehicleNumber);
    if (v.remarks) fd.append('remarks', v.remarks);
    try {
      await createHarvest.mutateAsync(fd);
      toast.success('Harvest recorded');
      form.reset();
      setBill(null);
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not record harvest');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Record harvest
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Record harvest</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="harvestDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Harvest date</FormLabel>
                  <FormControl>
                    <Input type="date" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
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
                  <FormLabel>Buyer (optional)</FormLabel>
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
                  <FormLabel>Price per kg (optional)</FormLabel>
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
                  <FormLabel>Vehicle number (optional)</FormLabel>
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
                  <FormLabel>Remarks (optional)</FormLabel>
                  <FormControl>
                    <Textarea rows={2} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="space-y-1">
              <label className="text-sm font-medium" htmlFor="bill-photo">
                Bill photo
              </label>
              <input
                id="bill-photo"
                ref={fileRef}
                type="file"
                accept="image/*"
                onChange={(e) => setBill(e.target.files?.[0] ?? null)}
                className="block w-full text-sm text-muted-foreground file:mr-3 file:rounded-md file:border-0 file:bg-primary/10 file:px-3 file:py-1.5 file:text-primary"
              />
              {bill && (
                <p className="text-xs text-muted-foreground">{bill.name}</p>
              )}
            </div>
            <Button
              type="submit"
              className="w-full"
              disabled={createHarvest.isPending}
            >
              {createHarvest.isPending ? 'Saving…' : 'Save harvest'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export function HarvestSection({ cycleId }: { cycleId: string }) {
  const harvests = useHarvests(cycleId);

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-base">Harvest</CardTitle>
        <RecordHarvestDialog cycleId={cycleId} />
      </CardHeader>
      <CardContent className="space-y-2">
        {harvests.data && harvests.data.length === 0 && (
          <p className="text-sm text-muted-foreground">
            No harvests recorded for this cycle.
          </p>
        )}
        {harvests.data?.map((h) => (
          <div
            key={h.id}
            className="flex items-center gap-3 rounded-lg border border-border p-3"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-500/15 text-emerald-600">
              <Fish className="h-4 w-4" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-medium">
                {formatKg(h.harvestQuantityKg)}
                {h.totalAmount != null && ` · ${formatCurrency(h.totalAmount)}`}
              </p>
              <p className="truncate text-xs text-muted-foreground">
                {formatDate(h.harvestDate)}
                {h.buyerName ? ` · ${h.buyerName}` : ''}
              </p>
            </div>
            {h.billPhotoPath && (
              <Receipt className="h-4 w-4 text-muted-foreground" />
            )}
            <Badge variant={h.status === 'ACTIVE' ? 'default' : 'secondary'}>
              {h.status}
            </Badge>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
