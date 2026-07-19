import { useState } from 'react';
import { Fish, IndianRupee, Scale, Waves } from 'lucide-react';
import {
  useHarvestAnalytics,
  useHistoryHarvests,
  usePondsBySite,
} from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import { SiteSelector } from '@/components/common/SiteSelector';
import { StatCard } from '@/components/common/StatCard';
import { EmptyBlock, LoadingBlock } from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatCurrency, formatDate, formatKg } from '@/lib/format';

export function HarvestedPage() {
  const { sites, siteId, select } = useSelectedSite();
  const ponds = usePondsBySite(siteId);
  const [pondId, setPondId] = useState<string | null>(null);
  const summary = useHarvestAnalytics(pondId);
  const harvests = useHistoryHarvests(pondId);

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      <Select value={pondId ?? undefined} onValueChange={setPondId}>
        <SelectTrigger>
          <SelectValue placeholder="Select a pond" />
        </SelectTrigger>
        <SelectContent>
          {(ponds.data ?? []).map((p) => (
            <SelectItem key={p.id} value={p.id}>
              {p.pondName} ({p.pondCode})
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {!pondId && (
        <EmptyBlock
          icon={<Waves className="h-6 w-6" />}
          title="Select a pond"
          description="Pick a pond to see its harvest totals and records."
        />
      )}

      {pondId && summary.data && (
        <div className="grid grid-cols-3 gap-2">
          <StatCard
            label="Harvests"
            value={String(summary.data.harvestCount)}
            icon={Fish}
          />
          <StatCard
            label="Total"
            value={formatKg(summary.data.totalHarvestKg)}
            icon={Scale}
          />
          <StatCard
            label="Revenue"
            value={formatCurrency(summary.data.totalRevenue)}
            icon={IndianRupee}
            tone="success"
          />
        </div>
      )}

      {pondId && (
        <div className="space-y-2">
          {harvests.isLoading && <LoadingBlock label="Loading harvests…" />}
          {harvests.data?.length === 0 && (
            <EmptyBlock
              icon={<Fish className="h-6 w-6" />}
              title="No harvests"
              description="This pond has no harvest records yet."
            />
          )}
          {harvests.data?.map((h) => (
            <Card key={h.harvestId}>
              <CardContent className="flex items-start gap-3 p-4">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-emerald-500/15 text-emerald-600">
                  <Fish className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="font-medium">
                    {formatKg(h.harvestQuantityKg)}
                    {h.sellingPricePerKg != null &&
                      ` @ ${formatCurrency(h.sellingPricePerKg)}/kg`}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Cycle #{h.cycleNumber} · {formatDate(h.harvestDate)}
                    {h.buyerName ? ` · ${h.buyerName}` : ''}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-semibold">{formatCurrency(h.totalAmount)}</p>
                  <Badge
                    variant={h.status === 'ACTIVE' ? 'default' : 'secondary'}
                    className="mt-1"
                  >
                    {h.status}
                  </Badge>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
