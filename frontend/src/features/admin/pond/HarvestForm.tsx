import { useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';

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

/**
 * The harvest capture form (fields + required bill photo). Builds the multipart
 * FormData and hands it to `onSubmit`; shared by the record dialog and the
 * guided "harvest & close cycle" flow so the fields never diverge.
 */
export function HarvestForm({
  cycleId,
  submitLabel,
  pending,
  onSubmit,
}: {
  cycleId: string;
  submitLabel: string;
  pending: boolean;
  onSubmit: (fd: FormData) => Promise<void>;
}) {
  const [bill, setBill] = useState<File | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);
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

  const submit = async (v: FormValues) => {
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
    await onSubmit(fd);
    form.reset();
    setBill(null);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(submit)} className="space-y-3">
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
          {bill && <p className="text-xs text-muted-foreground">{bill.name}</p>}
        </div>
        <Button type="submit" className="w-full" disabled={pending}>
          {pending ? 'Saving…' : submitLabel}
        </Button>
      </form>
    </Form>
  );
}
