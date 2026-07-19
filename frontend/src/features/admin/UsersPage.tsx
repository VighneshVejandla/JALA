import { UserRound } from 'lucide-react';
import { useUsers } from '@/api/queries';
import { ROLE_LABELS } from '@/auth/roles';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Avatar,
  AvatarFallback,
} from '@/components/ui/avatar';

export function UsersPage() {
  const { data, isLoading, isError, refetch } = useUsers();

  if (isLoading) return <LoadingBlock label="Loading users…" />;
  if (isError)
    return <ErrorBlock message="Could not load users." onRetry={() => refetch()} />;
  if (!data || data.length === 0)
    return <EmptyBlock icon={<UserRound className="h-6 w-6" />} title="No users" />;

  return (
    <div className="space-y-3">
      {data.map((u) => (
        <Card key={u.id}>
          <CardContent className="flex items-center gap-3 p-4">
            <Avatar className="h-10 w-10 border border-border">
              <AvatarFallback className="bg-primary/10 text-sm font-semibold text-primary">
                {u.fullName
                  .split(' ')
                  .map((p) => p[0])
                  .filter(Boolean)
                  .slice(0, 2)
                  .join('')
                  .toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium">{u.fullName}</p>
              <p className="truncate text-xs text-muted-foreground">
                {u.employeeCode}
                {u.email && ` · ${u.email}`}
              </p>
            </div>
            <div className="flex flex-col items-end gap-1">
              <Badge variant="secondary">{ROLE_LABELS[u.role]}</Badge>
              {!u.isActive && (
                <span className="text-xs text-muted-foreground">Inactive</span>
              )}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
