import { useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  Pill,
  Sprout,
  Wheat,
} from 'lucide-react';
import { usePondDashboard } from '@/api/queries';
import { FeedSection } from '@/features/admin/pond/FeedSection';
import { MedicineSection } from '@/features/admin/pond/MedicineSection';
import { StatCard } from '@/components/common/StatCard';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { formatDate, formatKg, formatNumber } from '@/lib/format';

export function PondDetailPage() {
  const { pondId } = useParams<{ pondId: string }>();
  const navigate = useNavigate();
  const { data, isLoading, isError, refetch } = usePondDashboard(pondId ?? null);

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" className="-ml-2" onClick={() => navigate(-1)}>
        <ArrowLeft className="mr-1 h-4 w-4" /> Back
      </Button>

      {isLoading && <LoadingBlock label="Loading pond…" />}
      {isError && (
        <ErrorBlock message="Could not load this pond." onRetry={() => refetch()} />
      )}

      {data && (
        <>
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-semibold">{data.pondName}</h2>
              {data.cycleStatus && (
                <Badge variant="secondary">{data.cycleStatus}</Badge>
              )}
            </div>
            <p className="text-sm text-muted-foreground">
              {data.pondCode} · {data.siteName}
            </p>
          </div>

          {/* No active cycle → a worker cannot record anything yet. */}
          {!(data.activeCycleId && data.cycleStatus === 'ACTIVE') && (
            <EmptyBlock
              icon={<Sprout className="h-6 w-6" />}
              title="Pond cycle has not started yet"
              description="Please contact the administrator to start a cycle for this pond."
            />
          )}

          {/* Workers record daily feed & medicine against the active cycle. */}
          {data.activeCycleId && data.cycleStatus === 'ACTIVE' && pondId && (
            <>
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">Current cycle</CardTitle>
                </CardHeader>
                <CardContent className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
                  <Detail label="Cycle #" value={formatNumber(data.cycleNumber)} />
                  <Detail label="Species" value={data.species ?? '—'} />
                  <Detail
                    label="Stocked on"
                    value={formatDate(data.stockingDate)}
                    icon={<Calendar className="h-3.5 w-3.5" />}
                  />
                  <Detail
                    label="Days since stocking"
                    value={formatNumber(data.daysSinceStocking)}
                  />
                  <Detail
                    label="Shrimp count"
                    value={formatNumber(data.shrimpCount)}
                  />
                </CardContent>
              </Card>

              <div className="grid grid-cols-2 gap-3">
                <StatCard
                  label="Today's Feed"
                  value={formatKg(data.todayFeedKg)}
                  icon={Wheat}
                  hint={`${formatNumber(data.todayFeedEntries)} entries`}
                />
                <StatCard
                  label="Medicine Entries"
                  value={formatNumber(data.medicineEntryCount)}
                  icon={Pill}
                />
              </div>

              <FeedSection
                cycleId={data.activeCycleId}
                pondId={pondId}
                canManageSessions={false}
              />
              <MedicineSection cycleId={data.activeCycleId} />
            </>
          )}
        </>
      )}
    </div>
  );
}

function Detail({
  label,
  value,
  icon,
}: {
  label: string;
  value: string;
  icon?: React.ReactNode;
}) {
  return (
    <div className="space-y-0.5">
      <p className="flex items-center gap-1 text-xs text-muted-foreground">
        {icon}
        {label}
      </p>
      <p className="font-medium text-foreground">{value}</p>
    </div>
  );
}
