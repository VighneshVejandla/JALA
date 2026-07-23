import { useNavigate } from 'react-router-dom';
import { Compass } from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';
import { homePathFor } from '@/auth/roles';
import { Button } from '@/components/ui/button';

export function NotFound() {
  const navigate = useNavigate();
  const { experience } = useAuth();

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 bg-background px-4 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-primary">
        <Compass className="h-8 w-8" />
      </div>
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">Page not found</h1>
        <p className="text-sm text-muted-foreground">
          The page you're looking for doesn't exist or has moved.
        </p>
      </div>
      <Button onClick={() => navigate(experience ? homePathFor(experience) : '/login')}>
        Go home
      </Button>
    </div>
  );
}
