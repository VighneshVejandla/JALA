import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight, Plus, Truck } from 'lucide-react';
import { toast } from 'sonner';
import { useCreateDelivery, useDeliveries } from '@/api/queries';
import { ROUTES } from '@/constants/routes';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { formatDateTime } from '@/lib/format';
import { cn } from '@/lib/utils';

function statusTone(status: string): string {
  switch (status.toUpperCase()) {
    case 'COMPLETED':
    case 'DELIVERED':
      return 'bg-emerald-500/15 text-emerald-700';
    case 'CANCELLED':
      return 'bg-destructive/15 text-destructive';
    default:
      return 'bg-primary/15 text-primary';
  }
}

export function DriverDeliveries() {
  const { data, isLoading, isError, refetch } = useDeliveries();
  const createDelivery = useCreateDelivery();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [remarks, setRemarks] = useState('');
  const [filter, setFilter] = useState<'all' | 'ACTIVE' | 'CANCELLED'>('all');

  const all = data ?? [];
  const filtered =
    filter === 'all' ? all : all.filter((d) => d.status.toUpperCase() === filter);
  // On the home tab (unfiltered), surface just the latest five.
  const deliveries = filter === 'all' ? filtered.slice(0, 5) : filtered;
  const truncated = filter === 'all' && filtered.length > deliveries.length;

  const submit = async () => {
    try {
      await createDelivery.mutateAsync({ remarks: remarks.trim() || undefined });
      toast.success('Delivery started');
      setRemarks('');
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not create delivery');
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">My deliveries</h2>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button size="sm">
              <Plus className="mr-1 h-4 w-4" /> New
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Start a delivery</DialogTitle>
              <DialogDescription>
                Record a new feed delivery run. You can add site drop-offs
                afterwards.
              </DialogDescription>
            </DialogHeader>
            <Textarea
              placeholder="Remarks (optional)"
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              rows={3}
            />
            <DialogFooter>
              <Button
                onClick={submit}
                disabled={createDelivery.isPending}
                className="w-full sm:w-auto"
              >
                {createDelivery.isPending ? 'Starting…' : 'Start delivery'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {all.length > 0 && (
        <div className="inline-flex rounded-lg border border-border p-0.5">
          {(['all', 'ACTIVE', 'CANCELLED'] as const).map((f) => (
            <button
              key={f}
              type="button"
              onClick={() => setFilter(f)}
              className={cn(
                'rounded-md px-3 py-1 text-sm font-medium capitalize transition-colors',
                filter === f
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground',
              )}
            >
              {f === 'all' ? 'All' : f.toLowerCase()}
            </button>
          ))}
        </div>
      )}

      {isLoading && <LoadingBlock label="Loading deliveries…" />}
      {isError && (
        <ErrorBlock message="Could not load deliveries." onRetry={() => refetch()} />
      )}
      {data && all.length === 0 && (
        <EmptyBlock
          icon={<Truck className="h-6 w-6" />}
          title="No deliveries yet"
          description="Start a delivery to begin recording drop-offs."
        />
      )}

      <div className="space-y-3">
        {deliveries.map((d) => (
          <Card
            key={d.id}
            role="button"
            tabIndex={0}
            onClick={() => navigate(ROUTES.driverDeliveryDetail(d.id))}
            onKeyDown={(e) => {
              if (e.key === 'Enter') navigate(ROUTES.driverDeliveryDetail(d.id));
            }}
            className="cursor-pointer transition-colors hover:border-primary/40"
          >
            <CardContent className="flex items-start gap-3 p-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <Truck className="h-5 w-5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="font-medium">
                  {d.deliveredBy ?? 'Delivery'}
                </p>
                <p className="text-xs text-muted-foreground">
                  {formatDateTime(d.deliveredAt)}
                </p>
                {d.remarks && (
                  <p className="mt-1 text-sm text-muted-foreground">{d.remarks}</p>
                )}
              </div>
              <Badge
                className={cn('shrink-0 border-0', statusTone(d.status))}
                variant="secondary"
              >
                {d.status}
              </Badge>
              <ChevronRight className="h-5 w-5 shrink-0 text-muted-foreground" />
            </CardContent>
          </Card>
        ))}
        {truncated && (
          <p className="text-center text-xs text-muted-foreground">
            Showing the latest 5 — use a filter to see all.
          </p>
        )}
      </div>
    </div>
  );
}
