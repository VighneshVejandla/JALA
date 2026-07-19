import { MapPin } from 'lucide-react';
import { useSites } from '@/api/queries';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { formatNumber } from '@/lib/format';

export function SitesPage() {
  const { data, isLoading, isError, refetch } = useSites();

  if (isLoading) return <LoadingBlock label="Loading sites…" />;
  if (isError)
    return <ErrorBlock message="Could not load sites." onRetry={() => refetch()} />;
  if (!data || data.length === 0)
    return <EmptyBlock icon={<MapPin className="h-6 w-6" />} title="No sites" />;

  return (
    <div className="space-y-3">
      {data.map((site) => (
        <Card key={site.id}>
          <CardContent className="flex items-start gap-3 p-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <MapPin className="h-5 w-5" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium">{site.siteName}</p>
              <p className="truncate text-xs text-muted-foreground">
                {site.siteCode}
                {site.location && ` · ${site.location}`}
              </p>
              <p className="text-xs text-muted-foreground">
                {site.ownerName ? `Owner: ${site.ownerName}` : ''}
                {site.totalAcres != null &&
                  ` · ${formatNumber(site.totalAcres)} acres`}
              </p>
            </div>
            <Badge variant={site.isActive ? 'default' : 'secondary'}>
              {site.isActive ? 'Active' : 'Inactive'}
            </Badge>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
