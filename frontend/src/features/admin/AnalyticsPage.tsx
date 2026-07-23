import { Fish, IndianRupee, PackageOpen, Utensils } from 'lucide-react';
import {
  useFeedChart,
  useHarvestChart,
  useInventory,
  useRevenueChart,
  useSiteFeedAnalytics,
  useSiteHarvestAnalytics,
} from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import { SiteSelector } from '@/components/common/SiteSelector';
import { StatCard } from '@/components/common/StatCard';
import { MonthlyChart } from '@/components/common/MonthlyChart';
import { LoadingBlock } from '@/components/common/StateViews';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { formatCurrency, formatKg, formatNumber } from '@/lib/format';

export function AnalyticsPage() {
  const { sites, siteId, select, isLoading: sitesLoading } = useSelectedSite();
  const feed = useSiteFeedAnalytics(siteId);
  const harvest = useSiteHarvestAnalytics(siteId);
  const inventory = useInventory(siteId);
  const revenueChart = useRevenueChart(siteId);
  const feedChart = useFeedChart(siteId);
  const harvestChart = useHarvestChart(siteId);

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      {sitesLoading && <LoadingBlock label="Loading analytics…" />}

      {feed.data && (
        <div className="grid grid-cols-2 gap-3">
          <StatCard
            label="Feed (month)"
            value={formatKg(feed.data.monthFeedKg)}
            icon={Utensils}
            hint={`${formatNumber(feed.data.monthFeedEntries)} entries`}
          />
          <StatCard
            label="Ponds fed (today)"
            value={formatNumber(feed.data.pondsFedToday)}
            icon={Utensils}
          />
        </div>
      )}

      {harvest.data && (
        <div className="grid grid-cols-2 gap-3">
          <StatCard
            label="Harvest (month)"
            value={formatKg(harvest.data.monthHarvestKg)}
            icon={Fish}
            tone="success"
          />
          <StatCard
            label="Revenue (month)"
            value={formatCurrency(harvest.data.monthRevenue)}
            icon={IndianRupee}
            tone="success"
          />
        </div>
      )}

      {inventory.data && (
        <div className="grid grid-cols-2 gap-3">
          <StatCard
            label="Available feed"
            value={formatKg(inventory.data.availableKg)}
            icon={PackageOpen}
            hint={`${formatNumber(inventory.data.availableBags)} bags`}
          />
          <StatCard
            label="Consumed (month)"
            value={formatKg(inventory.data.consumedMonthKg)}
            icon={PackageOpen}
          />
        </div>
      )}

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Revenue · last 12 months</CardTitle>
        </CardHeader>
        <CardContent>
          {revenueChart.isLoading ? (
            <LoadingBlock />
          ) : (
            <MonthlyChart data={revenueChart.data ?? []} />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Feed · last 12 months</CardTitle>
        </CardHeader>
        <CardContent>
          {feedChart.isLoading ? (
            <LoadingBlock />
          ) : (
            <MonthlyChart data={feedChart.data ?? []} color="hsl(199 89% 48%)" />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Harvest · last 12 months</CardTitle>
        </CardHeader>
        <CardContent>
          {harvestChart.isLoading ? (
            <LoadingBlock />
          ) : (
            <MonthlyChart data={harvestChart.data ?? []} color="hsl(142 71% 45%)" />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
