import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useActiveCycle, usePond } from '@/api/queries';
import { LoadingBlock } from '@/components/common/StateViews';
import { Button } from '@/components/ui/button';
import { CycleSection } from './CycleSection';
import { FeedSection } from './FeedSection';
import { MedicineSection } from './MedicineSection';
import { HarvestSection } from './HarvestSection';

/**
 * Admin pond workspace: manage the active cycle, feed sessions & entries,
 * medicine usage & photos, and the harvest lifecycle.
 */
export function PondManage() {
  const { pondId } = useParams<{ pondId: string }>();
  const navigate = useNavigate();
  const pond = usePond(pondId ?? null);
  const cycle = useActiveCycle(pondId ?? null);

  const activeCycle = cycle.data ?? null;
  const hasActive = !!activeCycle && activeCycle.status === 'ACTIVE';

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" className="-ml-2" onClick={() => navigate(-1)}>
        <ArrowLeft className="mr-1 h-4 w-4" /> Back
      </Button>

      {pond.isLoading && <LoadingBlock label="Loading pond…" />}

      {pond.data && (
        <div className="space-y-1">
          <h2 className="text-xl font-semibold">{pond.data.pondName}</h2>
          <p className="text-sm text-muted-foreground">
            {pond.data.pondCode} · {pond.data.siteName}
          </p>
        </div>
      )}

      {pondId && (
        <CycleSection
          pondId={pondId}
          cycle={activeCycle}
          isLoading={cycle.isLoading}
        />
      )}

      {hasActive && activeCycle && (
        <>
          <FeedSection cycleId={activeCycle.id} pondId={pondId!} />
          <MedicineSection cycleId={activeCycle.id} />
          <HarvestSection cycleId={activeCycle.id} />
        </>
      )}
    </div>
  );
}
