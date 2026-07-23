import { useState } from 'react';
import { FileBarChart } from 'lucide-react';
import { usePondsBySite } from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import type { ReportFilterRequest } from '@/api/types';
import { ExportBar } from './ExportBar';
import { SiteSelector } from '@/components/common/SiteSelector';
import { EmptyBlock } from '@/components/common/StateViews';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

function monthAgoIso() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().slice(0, 10);
}
function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

const ALL_PONDS = '__all__';

export function ReportsPage() {
  const { sites, siteId, select } = useSelectedSite();
  const ponds = usePondsBySite(siteId);
  const [pondId, setPondId] = useState<string>(ALL_PONDS);
  const [fromDate, setFromDate] = useState(monthAgoIso());
  const [toDate, setToDate] = useState(todayIso());

  const filter: ReportFilterRequest | null = siteId
    ? {
        siteId,
        pondId: pondId === ALL_PONDS ? undefined : pondId,
        fromDate,
        toDate,
      }
    : null;

  return (
    <div className="space-y-4">
      <SiteSelector sites={sites} siteId={siteId} onSelect={select} />

      {sites.length === 0 && (
        <EmptyBlock
          icon={<FileBarChart className="h-6 w-6" />}
          title="No sites"
          description="Reports are generated per site."
        />
      )}

      {siteId && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Report filters</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <label className="mb-1 block text-xs text-muted-foreground">
                Pond (optional)
              </label>
              <Select value={pondId} onValueChange={setPondId}>
                <SelectTrigger className="h-9">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_PONDS}>All ponds</SelectItem>
                  {(ponds.data ?? []).map((p) => (
                    <SelectItem key={p.id} value={p.id}>
                      {p.pondName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="mb-1 block text-xs text-muted-foreground">
                  From
                </label>
                <Input
                  type="date"
                  value={fromDate}
                  max={toDate}
                  onChange={(e) => setFromDate(e.target.value)}
                />
              </div>
              <div>
                <label className="mb-1 block text-xs text-muted-foreground">
                  To
                </label>
                <Input
                  type="date"
                  value={toDate}
                  min={fromDate}
                  onChange={(e) => setToDate(e.target.value)}
                />
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Download reports</CardTitle>
        </CardHeader>
        <CardContent>
          <ExportBar filter={filter} />
        </CardContent>
      </Card>
    </div>
  );
}
