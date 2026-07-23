import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { TrendingDown } from 'lucide-react';
import type { DailyFeedResponse } from '@/api/types';
import { formatKg } from '@/lib/format';

/**
 * Daily feed bars with a reduction alert: if today's feed dropped materially
 * versus yesterday, surface a pond-health nudge (the SRS "feed reduction
 * detection" requirement).
 */
export function FeedTrend({ data }: { data: DailyFeedResponse[] }) {
  const rows = data.map((d) => ({
    label: d.date.slice(5), // MM-DD
    value: d.feedKg,
  }));

  const n = data.length;
  const today = n >= 1 ? data[n - 1].feedKg : 0;
  const yesterday = n >= 2 ? data[n - 2].feedKg : 0;
  const dropPct =
    yesterday > 0 ? Math.round(((yesterday - today) / yesterday) * 100) : 0;
  const reduced = yesterday > 0 && dropPct >= 25;

  return (
    <div className="space-y-3">
      <div className="h-48 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={rows} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.2} />
            <XAxis dataKey="label" tickLine={false} axisLine={false} fontSize={10} />
            <YAxis tickLine={false} axisLine={false} fontSize={11} width={44} />
            <Tooltip
              contentStyle={{
                borderRadius: 8,
                border: '1px solid hsl(var(--border))',
                fontSize: 12,
              }}
            />
            <Bar
              dataKey="value"
              fill="hsl(var(--primary))"
              radius={[4, 4, 0, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {reduced && (
        <div className="flex items-start gap-2 rounded-lg border border-amber-500/40 bg-amber-500/5 p-3">
          <TrendingDown className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
          <p className="text-sm text-amber-700">
            Feed intake dropped {dropPct}% ({formatKg(yesterday)} →{' '}
            {formatKg(today)}). Please inspect pond health.
          </p>
        </div>
      )}
    </div>
  );
}
