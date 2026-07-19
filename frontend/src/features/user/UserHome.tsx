import {
  Bell,
  Fish,
  PackageOpen,
  TrendingUp,
  Waves,
  Wheat,
} from 'lucide-react';
import { useHomeDashboard } from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import { SiteSelector } from '@/components/common/SiteSelector';
import { StatCard } from '@/components/common/StatCard';
import { ErrorBlock, LoadingBlock } from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { formatCurrency, formatKg, formatNumber } from '@/lib/format';

export function UserHome() {
  const { sites, siteId, select, isLoading: sitesLoading } = useSelectedSite();
  const { data, isLoading, isError, refetch } = useHomeDashboard(siteId);

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      {(sitesLoading || isLoading) && <LoadingBlock label="Loading dashboard…" />}

      {isError && (
        <ErrorBlock
          message="Could not load this site's dashboard."
          onRetry={() => refetch()}
        />
      )}

      {data && (
        <>
          {data.lowInventory && (
            <Card className="border-amber-500/40 bg-amber-500/5">
              <CardContent className="flex items-center gap-3 p-4">
                <PackageOpen className="h-5 w-5 text-amber-600" />
                <p className="text-sm font-medium text-amber-700">
                  Feed inventory is running low at {data.siteName}.
                </p>
              </CardContent>
            </Card>
          )}

          <div className="grid grid-cols-2 gap-3">
            <StatCard
              label="Today's Feed"
              value={formatKg(data.todayFeedKg)}
              icon={Wheat}
            />
            <StatCard
              label="Available Feed"
              value={formatKg(data.availableFeedKg)}
              icon={PackageOpen}
              tone={data.lowInventory ? 'warning' : 'default'}
            />
            <StatCard
              label="Today's Harvest"
              value={formatKg(data.todayHarvestKg)}
              icon={Fish}
              tone="success"
            />
            <StatCard
              label="Today's Revenue"
              value={formatCurrency(data.todayRevenue)}
              icon={TrendingUp}
              tone="success"
            />
            <StatCard
              label="Total Ponds"
              value={formatNumber(data.totalPonds)}
              icon={Waves}
            />
            <StatCard
              label="Active Cycles"
              value={formatNumber(data.activeCycles)}
              icon={Waves}
            />
          </div>

          <Card>
            <CardContent className="flex items-center justify-between p-4">
              <div className="flex items-center gap-3">
                <Bell className="h-5 w-5 text-primary" />
                <span className="text-sm font-medium">Unread alerts</span>
              </div>
              <Badge variant={data.unreadNotifications > 0 ? 'default' : 'secondary'}>
                {formatNumber(data.unreadNotifications)}
              </Badge>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}
