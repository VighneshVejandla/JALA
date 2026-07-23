import { useState } from 'react';
import { Fish, Plus, Receipt } from 'lucide-react';
import { toast } from 'sonner';
import { useCreateHarvest, useHarvests } from '@/api/queries';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { formatCurrency, formatDate, formatKg } from '@/lib/format';
import { HarvestForm } from './HarvestForm';

function RecordHarvestDialog({ cycleId }: { cycleId: string }) {
  const [open, setOpen] = useState(false);
  const createHarvest = useCreateHarvest(cycleId);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Record harvest
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Record harvest</DialogTitle>
        </DialogHeader>
        <HarvestForm
          cycleId={cycleId}
          submitLabel="Save harvest"
          pending={createHarvest.isPending}
          onSubmit={async (fd) => {
            try {
              await createHarvest.mutateAsync(fd);
              toast.success('Harvest recorded');
              setOpen(false);
            } catch (err) {
              toast.error(
                err instanceof Error ? err.message : 'Could not record harvest',
              );
            }
          }}
        />
      </DialogContent>
    </Dialog>
  );
}

export function HarvestSection({ cycleId }: { cycleId: string }) {
  const harvests = useHarvests(cycleId);

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-base">Harvest</CardTitle>
        <RecordHarvestDialog cycleId={cycleId} />
      </CardHeader>
      <CardContent className="space-y-2">
        {harvests.data && harvests.data.length === 0 && (
          <p className="text-sm text-muted-foreground">
            No harvests recorded for this cycle.
          </p>
        )}
        {harvests.data?.map((h) => (
          <div
            key={h.id}
            className="flex items-center gap-3 rounded-lg border border-border p-3"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-500/15 text-emerald-600">
              <Fish className="h-4 w-4" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-medium">
                {formatKg(h.harvestQuantityKg)}
                {h.totalAmount != null && ` · ${formatCurrency(h.totalAmount)}`}
              </p>
              <p className="truncate text-xs text-muted-foreground">
                {formatDate(h.harvestDate)}
                {h.buyerName ? ` · ${h.buyerName}` : ''}
              </p>
            </div>
            {h.billPhotoPath && (
              <Receipt className="h-4 w-4 text-muted-foreground" />
            )}
            <Badge variant={h.status === 'ACTIVE' ? 'default' : 'secondary'}>
              {h.status}
            </Badge>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
