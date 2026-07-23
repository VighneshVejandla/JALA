import { useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation } from '@tanstack/react-query';
import { ImageIcon, ImagePlus, Pill, Plus } from 'lucide-react';
import { toast } from 'sonner';
import { api } from '@/api/endpoints';
import {
  useCreateMedicine,
  useMedicinePhotos,
  useMedicines,
} from '@/api/queries';
import type { MedicineUnit } from '@/api/types';
import { Button } from '@/components/ui/button';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatDateTime } from '@/lib/format';

const UNITS: MedicineUnit[] = ['KG', 'GM', 'LTR', 'ML'];

const schema = z.object({
  quantity: z.coerce.number().positive('Must be greater than 0'),
  unit: z.enum(['KG', 'GM', 'LTR', 'ML']),
  remarks: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

function AddMedicineDialog({ cycleId }: { cycleId: string }) {
  const [open, setOpen] = useState(false);
  const createMedicine = useCreateMedicine();
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { quantity: 0, unit: 'ML', remarks: '' },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await createMedicine.mutateAsync({
        pondCycleId: cycleId,
        quantity: v.quantity,
        unit: v.unit,
        remarks: v.remarks || undefined,
      });
      toast.success('Medicine recorded');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not save medicine');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Add medicine
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add medicine</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="quantity"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Quantity</FormLabel>
                  <FormControl>
                    <Input type="number" step="0.01" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="unit"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Unit</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {UNITS.map((u) => (
                        <SelectItem key={u} value={u}>
                          {u}
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
            <Button
              type="submit"
              className="w-full"
              disabled={createMedicine.isPending}
            >
              {createMedicine.isPending ? 'Saving…' : 'Save medicine'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

function UploadPhotoButton({ medicineEntryId }: { medicineEntryId: string }) {
  const inputRef = useRef<HTMLInputElement>(null);
  const upload = useMutation({
    mutationFn: (file: File) => {
      const form = new FormData();
      form.append('medicineEntryId', medicineEntryId);
      form.append('file', file);
      return api.medicinePhotos.upload(form);
    },
    onSuccess: () => toast.success('Photo uploaded'),
    onError: (e) =>
      toast.error(e instanceof Error ? e.message : 'Upload failed'),
  });

  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        aria-label="Medicine photo"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) upload.mutate(file);
          e.target.value = '';
        }}
      />
      <Button
        variant="ghost"
        size="sm"
        disabled={upload.isPending}
        onClick={() => inputRef.current?.click()}
      >
        <ImagePlus className="mr-1 h-4 w-4" />
        {upload.isPending ? 'Uploading…' : 'Photo'}
      </Button>
    </>
  );
}

function ViewPhotosDialog({ medicineEntryId }: { medicineEntryId: string }) {
  const [open, setOpen] = useState(false);
  const photos = useMedicinePhotos(open ? medicineEntryId : null);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="sm" aria-label="View photos">
          <ImageIcon className="mr-1 h-4 w-4" /> Photos
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Medicine photos</DialogTitle>
        </DialogHeader>
        {photos.isLoading && (
          <p className="text-sm text-muted-foreground">Loading…</p>
        )}
        {photos.data && photos.data.length === 0 && (
          <p className="text-sm text-muted-foreground">No photos uploaded yet.</p>
        )}
        <div className="grid grid-cols-2 gap-2">
          {photos.data?.map((p) => (
            <a
              key={p.id}
              href={p.filePath}
              target="_blank"
              rel="noreferrer"
              className="overflow-hidden rounded-lg border border-border"
            >
              <img
                src={p.filePath}
                alt={p.fileName}
                className="h-32 w-full object-cover"
                loading="lazy"
              />
            </a>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}

export function MedicineSection({ cycleId }: { cycleId: string }) {
  const medicines = useMedicines(cycleId);

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-base">Medicine</CardTitle>
        <AddMedicineDialog cycleId={cycleId} />
      </CardHeader>
      <CardContent className="space-y-2">
        {medicines.data && medicines.data.length === 0 && (
          <p className="text-sm text-muted-foreground">
            No medicine recorded for this cycle.
          </p>
        )}
        {medicines.data?.map((m) => (
          <div
            key={m.id}
            className="flex items-center gap-3 rounded-lg border border-border p-3"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <Pill className="h-4 w-4" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-medium">
                {m.quantity} {m.unit}
              </p>
              <p className="truncate text-xs text-muted-foreground">
                {formatDateTime(m.createdAt)}
                {m.remarks ? ` · ${m.remarks}` : ''}
              </p>
            </div>
            <ViewPhotosDialog medicineEntryId={m.id} />
            <UploadPhotoButton medicineEntryId={m.id} />
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
