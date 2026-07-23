import { Bell, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { ROLE_LABELS, homePathFor } from '@/auth/roles';
import { useNotifications } from '@/api/queries';
import { Button } from '@/components/ui/button';
import {
  Avatar,
  AvatarFallback,
} from '@/components/ui/avatar';

function initials(name: string): string {
  return name
    .split(' ')
    .map((p) => p[0])
    .filter(Boolean)
    .slice(0, 2)
    .join('')
    .toUpperCase();
}

export function TopBar({ title }: { title: string }) {
  const { user, experience, logout } = useAuth();
  const navigate = useNavigate();
  const notifications = useNotifications();
  const unread = notifications.data?.unreadCount ?? 0;

  return (
    <header
      className="sticky top-0 z-30 flex items-center justify-between gap-3 border-b border-border bg-background/95 px-4 py-3 backdrop-blur supports-[backdrop-filter]:bg-background/80"
      style={{ paddingTop: 'max(0.75rem, env(safe-area-inset-top))' }}
    >
      <div className="min-w-0">
        <h1 className="truncate text-lg font-semibold text-foreground">{title}</h1>
        {user && (
          <p className="truncate text-xs text-muted-foreground">
            {user.fullName} · {ROLE_LABELS[user.role]}
          </p>
        )}
      </div>
      <div className="flex items-center gap-1">
        {experience && (
          <Button
            variant="ghost"
            size="icon"
            aria-label={`Alerts${unread > 0 ? ` (${unread} unread)` : ''}`}
            className="relative"
            onClick={() => navigate(`${homePathFor(experience)}/alerts`)}
          >
            <Bell className="h-5 w-5" />
            {unread > 0 && (
              <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-semibold text-destructive-foreground">
                {unread > 9 ? '9+' : unread}
              </span>
            )}
          </Button>
        )}
        {user && (
          <Avatar className="h-9 w-9 border border-border">
            <AvatarFallback className="bg-primary/10 text-sm font-semibold text-primary">
              {initials(user.fullName)}
            </AvatarFallback>
          </Avatar>
        )}
        <Button
          variant="ghost"
          size="icon"
          aria-label="Sign out"
          onClick={logout}
        >
          <LogOut className="h-5 w-5" />
        </Button>
      </div>
    </header>
  );
}
