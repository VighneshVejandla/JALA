import { useMutation } from '@tanstack/react-query';
import { Download } from 'lucide-react';
import { toast } from 'sonner';
import { api } from '@/api/endpoints';
import type { ReportFilterRequest } from '@/api/types';
import { saveBlob } from '@/lib/download';
import { Button } from '@/components/ui/button';

export type ExportKind = 'revenue' | 'feed' | 'medicine';

/** Excel/PDF export buttons for the revenue/feed/medicine reports. */
export function ExportBar({
  filter,
  kinds = ['revenue', 'feed', 'medicine'],
}: {
  filter: ReportFilterRequest | null;
  kinds?: ExportKind[];
}) {
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
