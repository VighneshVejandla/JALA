import { useNavigate } from 'react-router-dom';
import { ShieldX } from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';
import { homePathFor } from '@/auth/roles';
import { Button } from '@/components/ui/button';

/** 403 — the user is authenticated but not allowed here. */
export function Forbidden() {
  const navigate = useNavigate();
  const { experience } = useAuth();

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 bg-background px-4 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-destructive/10 text-destructive">
        <ShieldX className="h-8 w-8" />
      </div>
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">Access denied</h1>
        <p className="text-sm text-muted-foreground">
          You don't have permission to view this page.
        </p>
      </div>
      <Button onClick={() => navigate(experience ? homePathFor(experience) : '/login')}>
        Go home
      </Button>
    </div>
  );
}
