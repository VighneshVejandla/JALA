import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronRight, Truck } from 'lucide-react';
import { useDeliveries } from '@/api/queries';
import { ROUTES } from '@/constants/routes';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { formatDateTime } from '@/lib/format';

type StatusFilter = 'all' | 'ACTIVE' | 'CANCELLED';

function tone(status: string): string {
  switch (status.toUpperCase()) {
    case 'CANCELLED':
      return 'bg-destructive/15 text-destructive';
    default:
      return 'bg-primary/15 text-primary';
  }
}

export function AdminDeliveriesPage() {
  const { data, isLoading, isError, refetch } = useDeliveries();
  const navigate = useNavigate();
  const [filter, setFilter] = useState<StatusFilter>('all');

  if (isLoading) return <LoadingBlock label="Loading deliveries…" />;
  if (isError)
    return (
      <ErrorBlock message="Could not load deliveries." onRetry={() => refetch()} />
    );

  const all = data ?? [];
  const deliveries =
    filter === 'all' ? all : all.filter((d) => d.status.toUpperCase() === filter);

  return (
    <div className="space-y-3">
      <div className="inline-flex rounded-lg border border-border p-0.5">
        {(['all', 'ACTIVE', 'CANCELLED'] as StatusFilter[]).map((f) => (
          <button
            key={f}
            type="button"
            onClick={() => setFilter(f)}
            className={cn(
              'rounded-md px-3 py-1 text-sm font-medium capitalize transition-colors',
              filter === f ? 'bg-primary text-primary-foreground' : 'text-muted-foreground',
            )}
          >
            {f === 'all' ? 'All' : f.toLowerCase()}
          </button>
        ))}
      </div>

      {all.length === 0 && (
        <EmptyBlock icon={<Truck className="h-6 w-6" />} title="No deliveries" />
      )}
      {all.length > 0 && deliveries.length === 0 && (
        <EmptyBlock icon={<Truck className="h-6 w-6" />} title="No matching deliveries" />
      )}

      {deliveries.map((d) => (
        <Card
          key={d.id}
          role="button"
          tabIndex={0}
          onClick={() => navigate(ROUTES.adminDeliveryDetail(d.id))}
          onKeyDown={(e) => {
            if (e.key === 'Enter') navigate(ROUTES.adminDeliveryDetail(d.id));
          }}
          className="cursor-pointer transition-colors hover:border-primary/40"
        >
          <CardContent className="flex items-start gap-3 p-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <Truck className="h-5 w-5" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="font-medium">{d.deliveredBy ?? 'Delivery'}</p>
              <p className="text-xs text-muted-foreground">
                {formatDateTime(d.deliveredAt)}
              </p>
              {d.remarks && (
                <p className="mt-1 text-sm text-muted-foreground">{d.remarks}</p>
              )}
            </div>
            <Badge className={cn('shrink-0 border-0', tone(d.status))} variant="secondary">
              {d.status}
            </Badge>
            <ChevronRight className="h-5 w-5 shrink-0 text-muted-foreground" />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
