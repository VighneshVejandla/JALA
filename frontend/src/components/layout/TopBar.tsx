import { LogOut } from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';
import { ROLE_LABELS } from '@/auth/roles';
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
  const { user, logout } = useAuth();

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
      <div className="flex items-center gap-2">
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
