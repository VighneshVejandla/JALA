import { ServerCrash } from 'lucide-react';
import { Button } from '@/components/ui/button';

/** 500 — an unexpected client crash or server error. */
export function ServerError({ onReset }: { onReset?: () => void }) {
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 bg-background px-4 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-destructive/10 text-destructive">
        <ServerCrash className="h-8 w-8" />
      </div>
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold">Something went wrong</h1>
        <p className="text-sm text-muted-foreground">
          An unexpected error occurred. Please try again.
        </p>
      </div>
      <Button onClick={() => (onReset ? onReset() : window.location.reload())}>
        Reload
      </Button>
    </div>
  );
}
