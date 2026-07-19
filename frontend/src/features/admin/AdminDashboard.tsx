import {
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
import { formatCurrency, formatKg, formatNumber } from '@/lib/format';

export function AdminDashboard() {
  const { sites, siteId, select, isLoading: sitesLoading } = useSelectedSite();
  const { data, isLoading, isError, refetch } = useHomeDashboard(siteId);

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-3">
        <StatCard
          label="Sites"
          value={formatNumber(sites.length)}
          icon={Waves}
        />
        <StatCard
          label="Active Cycles"
          value={data ? formatNumber(data.activeCycles) : '—'}
          icon={Waves}
        />
      </div>

      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      {(sitesLoading || isLoading) && <LoadingBlock label="Loading site dashboard…" />}
      {isError && (
        <ErrorBlock message="Could not load the dashboard." onRetry={() => refetch()} />
      )}

      {data && (
        <>
          {data.lowInventory && (
            <Card className="border-amber-500/40 bg-amber-500/5">
              <CardContent className="flex items-center gap-3 p-4">
                <PackageOpen className="h-5 w-5 text-amber-600" />
                <p className="text-sm font-medium text-amber-700">
                  Feed inventory is low at {data.siteName}.
                </p>
              </CardContent>
            </Card>
          )}
          <div className="grid grid-cols-2 gap-3">
            <StatCard label="Today's Feed" value={formatKg(data.todayFeedKg)} icon={Wheat} />
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
          </div>
        </>
      )}
    </div>
  );
}
