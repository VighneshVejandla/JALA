import { MapPin } from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { SiteResponse } from '@/api/types';

export function SiteSelector({
  sites,
  siteId,
  onSelect,
}: {
  sites: SiteResponse[];
  siteId: string | null;
  onSelect: (id: string) => void;
}) {
  if (sites.length === 0) return null;

  return (
    <div className="flex items-center gap-2">
      <MapPin className="h-4 w-4 shrink-0 text-muted-foreground" />
      <Select value={siteId ?? undefined} onValueChange={onSelect}>
        <SelectTrigger className="h-9 w-full">
          <SelectValue placeholder="Select a site" />
        </SelectTrigger>
        <SelectContent>
          {sites.map((s) => (
            <SelectItem key={s.id} value={s.id}>
              {s.siteName} ({s.siteCode})
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
