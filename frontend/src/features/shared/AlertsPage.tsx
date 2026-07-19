import { Bell, BellOff, Check } from 'lucide-react';
import { useMarkNotificationRead, useNotifications } from '@/api/queries';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { cn } from '@/lib/utils';
import { formatDateTime } from '@/lib/format';

export function AlertsPage() {
  const { data, isLoading, isError, refetch } = useNotifications();
  const markRead = useMarkNotificationRead();

  if (isLoading) return <LoadingBlock label="Loading alerts…" />;
  if (isError)
    return <ErrorBlock message="Could not load alerts." onRetry={() => refetch()} />;

  const notifications = data?.notifications ?? [];

  if (notifications.length === 0)
    return (
      <EmptyBlock
        icon={<BellOff className="h-6 w-6" />}
        title="No alerts"
        description="You're all caught up."
      />
    );

  return (
    <div className="space-y-3">
      {notifications.map((n) => {
        const unread = n.status === 'UNREAD';
        return (
          <Card key={n.id} className={cn(unread && 'border-primary/40 bg-primary/5')}>
            <CardContent className="flex items-start gap-3 p-4">
              <div
                className={cn(
                  'mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full',
                  unread ? 'bg-primary/15 text-primary' : 'bg-muted text-muted-foreground',
                )}
              >
                <Bell className="h-4 w-4" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="font-medium">{n.title}</p>
                <p className="text-sm text-muted-foreground">{n.message}</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  {formatDateTime(n.createdAt)}
                </p>
              </div>
              {unread && (
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Mark as read"
                  disabled={markRead.isPending}
                  onClick={() => markRead.mutate(n.id)}
                >
                  <Check className="h-4 w-4" />
                </Button>
              )}
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
