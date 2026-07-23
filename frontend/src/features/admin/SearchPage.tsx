import { useState } from 'react';
import { Search } from 'lucide-react';
import { useSearch } from '@/api/queries';
import type { GlobalSearchResponse, SearchResultResponse } from '@/api/types';
import { EmptyBlock, LoadingBlock } from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';

const GROUPS: { key: keyof GlobalSearchResponse; label: string }[] = [
  { key: 'users', label: 'Users' },
  { key: 'sites', label: 'Sites' },
  { key: 'ponds', label: 'Ponds' },
  { key: 'feedEntries', label: 'Feed entries' },
  { key: 'medicineEntries', label: 'Medicine' },
  { key: 'harvests', label: 'Harvests' },
  { key: 'notifications', label: 'Notifications' },
];

export function SearchPage() {
  const [keyword, setKeyword] = useState('');
  const { data, isLoading, isFetching } = useSearch(keyword);

  const totalResults = data
    ? GROUPS.reduce((sum, g) => sum + (data[g.key]?.length ?? 0), 0)
    : 0;

  return (
    <div className="space-y-4">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          className="pl-9"
          placeholder="Search sites, ponds, feed, medicine, harvests…"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          autoFocus
        />
      </div>

      {keyword.trim().length > 0 && keyword.trim().length < 2 && (
        <p className="text-sm text-muted-foreground">Type at least 2 characters.</p>
      )}

      {(isLoading || isFetching) && keyword.trim().length >= 2 && (
        <LoadingBlock label="Searching…" />
      )}

      {data && totalResults === 0 && (
        <EmptyBlock
          icon={<Search className="h-6 w-6" />}
          title="No matches"
          description={`Nothing found for "${keyword.trim()}".`}
        />
      )}

      {data &&
        totalResults > 0 &&
        GROUPS.map((g) => {
          const items = data[g.key] ?? [];
          if (items.length === 0) return null;
          return (
            <div key={g.key} className="space-y-2">
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-semibold">{g.label}</h3>
                <Badge variant="secondary">{items.length}</Badge>
              </div>
              {items.map((r: SearchResultResponse) => (
                <Card key={`${r.type}-${r.id}`}>
                  <CardContent className="p-3">
                    <p className="text-sm font-medium">{r.title}</p>
                    {r.subtitle && (
                      <p className="text-xs text-muted-foreground">{r.subtitle}</p>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          );
        })}
    </div>
  );
}
