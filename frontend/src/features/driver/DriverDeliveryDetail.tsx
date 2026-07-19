import { useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, ImagePlus, MapPin, Plus, Receipt } from 'lucide-react';
import { toast } from 'sonner';
import {
  useAddSiteDelivery,
  useDelivery,
  useDeliveryReceipts,
  useDeliverySites,
  useSites,
  useUploadReceipt,
} from '@/api/queries';
import type { SiteDeliveryResponse } from '@/api/types';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
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
import { formatDateTime, formatKg, formatNumber } from '@/lib/format';

const schema = z.object({
  siteId: z.string().min(1, 'Select a site'),
  numberOfBags: z.coerce.number().int().positive('Must be greater than 0'),
  remarks: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;

function AddDropDialog({ deliveryId }: { deliveryId: string }) {
  const [open, setOpen] = useState(false);
  const sites = useSites();
  const addDrop = useAddSiteDelivery(deliveryId);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { siteId: '', numberOfBags: 0, remarks: '' },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await addDrop.mutateAsync({
        siteId: v.siteId,
        numberOfBags: v.numberOfBags,
        remarks: v.remarks || undefined,
      });
      toast.success('Drop-off added');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not add drop-off');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Add drop-off
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add site drop-off</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="siteId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Site</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a site" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {(sites.data ?? []).map((s) => (
                        <SelectItem key={s.id} value={s.id}>
                          {s.siteName} ({s.siteCode})
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
              name="numberOfBags"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Number of bags</FormLabel>
                  <FormControl>
                    <Input type="number" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={addDrop.isPending}>
              {addDrop.isPending ? 'Adding…' : 'Add drop-off'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

function DropRow({ drop }: { drop: SiteDeliveryResponse }) {
  const receipts = useDeliveryReceipts(drop.id);
  const upload = useUploadReceipt(drop.id);
  const inputRef = useRef<HTMLInputElement>(null);

  const onFile = (file: File) => {
    const form = new FormData();
    form.append('siteDeliveryId', drop.id);
    form.append('file', file);
    upload.mutate(form, {
      onSuccess: () => toast.success('Receipt uploaded'),
      onError: (e) =>
        toast.error(e instanceof Error ? e.message : 'Upload failed'),
    });
  };

  return (
    <Card>
      <CardContent className="flex items-start gap-3 p-4">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <MapPin className="h-5 w-5" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate font-medium">{drop.siteName}</p>
          <p className="truncate text-xs text-muted-foreground">
            {drop.siteCode} · {formatNumber(drop.numberOfBags)} bags
            {drop.totalKg != null && ` · ${formatKg(drop.totalKg)}`}
          </p>
          <div className="mt-1 flex items-center gap-2">
            <Receipt className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs text-muted-foreground">
              {formatNumber(receipts.data?.length ?? 0)} receipt(s)
            </span>
          </div>
        </div>
        <input
          ref={inputRef}
          type="file"
          accept="image/*"
          className="hidden"
          aria-label={`Receipt for ${drop.siteName}`}
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) onFile(file);
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
          {upload.isPending ? 'Uploading…' : 'Receipt'}
        </Button>
      </CardContent>
    </Card>
  );
}

export function DriverDeliveryDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const delivery = useDelivery(id ?? null);
  const sites = useDeliverySites(id ?? null);

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" className="-ml-2" onClick={() => navigate(-1)}>
        <ArrowLeft className="mr-1 h-4 w-4" /> Deliveries
      </Button>

      {delivery.isLoading && <LoadingBlock label="Loading delivery…" />}
      {delivery.isError && (
        <ErrorBlock
          message="Could not load this delivery."
          onRetry={() => delivery.refetch()}
        />
      )}

      {delivery.data && (
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-semibold">Delivery</h2>
            <Badge variant="secondary">{delivery.data.status}</Badge>
          </div>
          <p className="text-sm text-muted-foreground">
            {delivery.data.deliveredBy ?? '—'} ·{' '}
            {formatDateTime(delivery.data.deliveredAt)}
          </p>
          {delivery.data.remarks && (
            <p className="text-sm text-muted-foreground">{delivery.data.remarks}</p>
          )}
        </div>
      )}

      <div className="flex items-center justify-between pt-2">
        <h3 className="font-semibold">Drop-offs</h3>
        {id && <AddDropDialog deliveryId={id} />}
      </div>

      {sites.isLoading && <LoadingBlock label="Loading drop-offs…" />}
      {sites.data && sites.data.length === 0 && (
        <EmptyBlock
          icon={<MapPin className="h-6 w-6" />}
          title="No drop-offs yet"
          description="Add a site drop-off and upload its receipt."
        />
      )}

      <div className="space-y-3">
        {sites.data?.map((drop) => (
          <DropRow key={drop.id} drop={drop} />
        ))}
      </div>
    </div>
  );
}
