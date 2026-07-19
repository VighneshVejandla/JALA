import { useNavigate } from 'react-router-dom';
import { ChevronRight, Waves } from 'lucide-react';
import { usePondsBySite } from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import { SiteSelector } from '@/components/common/SiteSelector';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

export function PondsPage() {
  const { sites, siteId, select } = useSelectedSite();
  const { data, isLoading, isError, refetch } = usePondsBySite(siteId);
  const navigate = useNavigate();

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      {isLoading && <LoadingBlock label="Loading ponds…" />}
      {isError && (
        <ErrorBlock message="Could not load ponds." onRetry={() => refetch()} />
      )}
      {data && data.length === 0 && (
        <EmptyBlock
          icon={<Waves className="h-6 w-6" />}
          title="No ponds"
          description="There are no ponds configured for this site yet."
        />
      )}

      <div className="space-y-3">
        {data?.map((pond) => (
          <Card
            key={pond.id}
            role="button"
            tabIndex={0}
            onClick={() => navigate(`/app/ponds/${pond.id}`)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') navigate(`/app/ponds/${pond.id}`);
            }}
            className="cursor-pointer transition-colors hover:border-primary/40"
          >
            <CardContent className="flex items-center gap-3 p-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <Waves className="h-5 w-5" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{pond.pondName}</p>
                <p className="truncate text-xs text-muted-foreground">
                  {pond.pondCode}
                  {pond.pondAcres != null && ` · ${pond.pondAcres} acres`}
                </p>
              </div>
              <Badge variant={pond.isActive ? 'default' : 'secondary'}>
                {pond.isActive ? 'Active' : 'Inactive'}
              </Badge>
              <ChevronRight className="h-5 w-5 text-muted-foreground" />
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
