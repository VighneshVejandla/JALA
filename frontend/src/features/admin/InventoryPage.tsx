import { AlertTriangle, PackageOpen } from 'lucide-react';
import { useFeedInventoryList } from '@/api/queries';
import type { FeedInventoryResponse } from '@/api/types';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { formatKg } from '@/lib/format';

/** Flag a site as low when less than 20% of received feed remains. */
function isLow(inv: FeedInventoryResponse): boolean {
  if (inv.totalReceivedKg <= 0) return false;
  return inv.availableKg / inv.totalReceivedKg < 0.2;
}

export function InventoryPage() {
  const { data, isLoading, isError, refetch } = useFeedInventoryList();

  if (isLoading) return <LoadingBlock label="Loading inventory…" />;
  if (isError)
    return (
      <ErrorBlock message="Could not load inventory." onRetry={() => refetch()} />
    );
  if (!data || data.length === 0)
    return (
      <EmptyBlock
        icon={<PackageOpen className="h-6 w-6" />}
        title="No inventory"
        description="Feed inventory appears once deliveries are recorded."
      />
    );

  return (
    <div className="space-y-3">
      {data.map((inv) => {
        const low = isLow(inv);
        const consumedPct =
          inv.totalReceivedKg > 0
            ? Math.min(100, (inv.totalConsumedKg / inv.totalReceivedKg) * 100)
            : 0;
        return (
          <Card key={inv.id} className={low ? 'border-amber-500/40' : undefined}>
            <CardContent className="space-y-3 p-4">
              <div className="flex items-start gap-3">
                <div
                  className={
                    low
                      ? 'flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-amber-600'
                      : 'flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary'
                  }
                >
                  {low ? (
                    <AlertTriangle className="h-5 w-5" />
                  ) : (
                    <PackageOpen className="h-5 w-5" />
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <p className="truncate font-medium">{inv.siteName}</p>
                    {low && (
                      <Badge className="border-0 bg-amber-500/15 text-amber-700">
                        Low stock
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground">{inv.siteCode}</p>
                </div>
                <div className="text-right">
                  <p className="text-lg font-semibold">
                    {formatKg(inv.availableKg)}
                  </p>
                  <p className="text-xs text-muted-foreground">available</p>
                </div>
              </div>
              <Progress value={consumedPct} />
              <div className="flex justify-between text-xs text-muted-foreground">
                <span>Received {formatKg(inv.totalReceivedKg)}</span>
                <span>Consumed {formatKg(inv.totalConsumedKg)}</span>
              </div>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
