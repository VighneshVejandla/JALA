import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Download, Waves } from 'lucide-react';
import { toast } from 'sonner';
import { api } from '@/api/endpoints';
import {
  useHistoryCycles,
  useHistoryFeeds,
  useHistoryHarvests,
  useHistoryMedicines,
  usePondsBySite,
} from '@/api/queries';
import { useSelectedSite } from '@/hooks/useSelectedSite';
import type { ReportFilterRequest } from '@/api/types';
import { FEED_SIZE_LABELS } from '@/api/types';
import { saveBlob } from '@/lib/download';
import { SiteSelector } from '@/components/common/SiteSelector';
import { EmptyBlock, LoadingBlock } from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  formatCurrency,
  formatDate,
  formatKg,
  formatNumber,
} from '@/lib/format';

function monthAgoIso() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().slice(0, 10);
}
function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

type ExportKind = 'revenue' | 'feed' | 'medicine';

function ExportBar({ filter }: { filter: ReportFilterRequest | null }) {
  const exportMut = useMutation({
    mutationFn: async ({
      kind,
      format,
    }: {
      kind: ExportKind;
      format: 'excel' | 'pdf';
    }) => {
      if (!filter) throw new Error('Select a site and date range first');
      const key = `${kind}${format === 'excel' ? 'Excel' : 'Pdf'}` as
        | 'revenueExcel'
        | 'revenuePdf'
        | 'feedExcel'
        | 'feedPdf'
        | 'medicineExcel'
        | 'medicinePdf';
      const blob = await api.exports[key](filter);
      const ext = format === 'excel' ? 'xlsx' : 'pdf';
      saveBlob(blob, `${kind}-report.${ext}`);
    },
    onError: (e) =>
      toast.error(e instanceof Error ? e.message : 'Download failed'),
  });

  const kinds: ExportKind[] = ['revenue', 'feed', 'medicine'];

  return (
    <div className="space-y-2">
      {kinds.map((kind) => (
        <div key={kind} className="flex items-center gap-2">
          <span className="w-20 text-sm capitalize text-muted-foreground">
            {kind}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={!filter || exportMut.isPending}
            onClick={() => exportMut.mutate({ kind, format: 'excel' })}
          >
            <Download className="mr-1 h-4 w-4" /> Excel
          </Button>
          <Button
            variant="outline"
            size="sm"
            disabled={!filter || exportMut.isPending}
            onClick={() => exportMut.mutate({ kind, format: 'pdf' })}
          >
            <Download className="mr-1 h-4 w-4" /> PDF
          </Button>
        </div>
      ))}
    </div>
  );
}

export function HistoryPage() {
  const { sites, siteId, select } = useSelectedSite();
  const ponds = usePondsBySite(siteId);
  const [pondId, setPondId] = useState<string | null>(null);
  const [fromDate, setFromDate] = useState(monthAgoIso());
  const [toDate, setToDate] = useState(todayIso());

  const cycles = useHistoryCycles(pondId);
  const feeds = useHistoryFeeds(pondId);
  const medicines = useHistoryMedicines(pondId);
  const harvests = useHistoryHarvests(pondId);

  const filter: ReportFilterRequest | null = siteId
    ? { siteId, pondId: pondId ?? undefined, fromDate, toDate }
    : null;

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

      <Card>
        <CardContent className="space-y-3 p-4">
          <p className="text-sm font-medium">Download reports</p>
          <div className="grid grid-cols-2 gap-2">
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground" htmlFor="from">
                From
              </label>
              <Input
                id="from"
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground" htmlFor="to">
                To
              </label>
              <Input
                id="to"
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
              />
            </div>
          </div>
          <ExportBar filter={filter} />
        </CardContent>
      </Card>

      {!pondId && (
        <EmptyBlock
          icon={<Waves className="h-6 w-6" />}
          title="Select a pond"
          description="Pick a pond to view its cycle, feed, medicine and harvest history."
        />
      )}

      {pondId && (
        <Tabs defaultValue="cycles">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="cycles">Cycles</TabsTrigger>
            <TabsTrigger value="feeds">Feeds</TabsTrigger>
            <TabsTrigger value="medicines">Medicine</TabsTrigger>
            <TabsTrigger value="harvests">Harvests</TabsTrigger>
          </TabsList>

          <TabsContent value="cycles" className="space-y-2 pt-3">
            {cycles.isLoading && <LoadingBlock />}
            {cycles.data?.length === 0 && <p className="text-sm text-muted-foreground">No cycles.</p>}
            {cycles.data?.map((c) => (
              <Card key={c.cycleId}>
                <CardContent className="flex items-center justify-between p-3 text-sm">
                  <div>
                    <p className="font-medium">
                      Cycle #{c.cycleNumber} · {c.species}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Stocked {formatDate(c.stockingDate)} ·{' '}
                      {formatNumber(c.totalFeedEntries)} feeds ·{' '}
                      {formatNumber(c.totalHarvests)} harvests
                    </p>
                  </div>
                  <Badge variant={c.currentCycle ? 'default' : 'secondary'}>
                    {c.status}
                  </Badge>
                </CardContent>
              </Card>
            ))}
          </TabsContent>

          <TabsContent value="feeds" className="space-y-2 pt-3">
            {feeds.isLoading && <LoadingBlock />}
            {feeds.data?.length === 0 && <p className="text-sm text-muted-foreground">No feed history.</p>}
            {feeds.data?.map((f) => (
              <Card key={f.feedEntryId}>
                <CardContent className="flex items-center justify-between p-3 text-sm">
                  <span>
                    {formatDate(f.feedDate)} · session {f.sessionNumber} · size{' '}
                    {FEED_SIZE_LABELS[f.feedSize]}
                  </span>
                  <span className="font-medium">{formatKg(f.feedQuantityKg)}</span>
                </CardContent>
              </Card>
            ))}
          </TabsContent>

          <TabsContent value="medicines" className="space-y-2 pt-3">
            {medicines.isLoading && <LoadingBlock />}
            {medicines.data?.length === 0 && <p className="text-sm text-muted-foreground">No medicine history.</p>}
            {medicines.data?.map((m) => (
              <Card key={m.medicineId}>
                <CardContent className="p-3 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="font-medium">
                      {m.quantity} {m.unit}
                    </span>
                    <span className="text-xs text-muted-foreground">
                      {formatDate(m.createdAt)}
                    </span>
                  </div>
                  {m.photos.length > 0 && (
                    <p className="mt-1 text-xs text-muted-foreground">
                      {formatNumber(m.photos.length)} photo(s)
                    </p>
                  )}
                </CardContent>
              </Card>
            ))}
          </TabsContent>

          <TabsContent value="harvests" className="space-y-2 pt-3">
            {harvests.isLoading && <LoadingBlock />}
            {harvests.data?.length === 0 && <p className="text-sm text-muted-foreground">No harvest history.</p>}
            {harvests.data?.map((h) => (
              <Card key={h.harvestId}>
                <CardContent className="flex items-center justify-between p-3 text-sm">
                  <div>
                    <p className="font-medium">{formatKg(h.harvestQuantityKg)}</p>
                    <p className="text-xs text-muted-foreground">
                      {formatDate(h.harvestDate)}
                      {h.buyerName ? ` · ${h.buyerName}` : ''}
                    </p>
                  </div>
                  <span className="font-medium">
                    {formatCurrency(h.totalAmount)}
                  </span>
                </CardContent>
              </Card>
            ))}
          </TabsContent>
        </Tabs>
      )}
    </div>
  );
}
