import { MapPin, PackageOpen, Utensils } from 'lucide-react';
import { useSiteFeedDaily, useHomeDashboard } from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import { SiteSelector } from '@/components/common/SiteSelector';
import { StatCard } from '@/components/common/StatCard';
import { FeedTrend } from '@/components/common/FeedTrend';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { formatKg } from '@/lib/format';

/**
 * Worker home is intentionally feed-focused: today's feed, available feed and
 * the feed trend. Harvest/revenue/pond-count widgets are admin concerns.
 */
export function UserHome() {
  const { sites, siteId, select, isLoading: sitesLoading } = useSelectedSite();
  const { data, isLoading, isError, refetch } = useHomeDashboard(siteId);
  const feedDaily = useSiteFeedDaily(siteId);

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      {!sitesLoading && sites.length === 0 && (
        <EmptyBlock
          icon={<MapPin className="h-6 w-6" />}
          title="No sites assigned"
          description="You don't have access to any sites yet. Contact your administrator."
        />
      )}

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
              icon={Utensils}
            />
            <StatCard
              label="Available Feed"
              value={formatKg(data.availableFeedKg)}
              icon={PackageOpen}
              tone={data.lowInventory ? 'warning' : 'default'}
            />
          </div>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base">Feed trend · last 14 days</CardTitle>
            </CardHeader>
            <CardContent>
              {feedDaily.isLoading ? (
                <LoadingBlock />
              ) : (
                <FeedTrend data={feedDaily.data ?? []} />
              )}
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}
