import type { ReactElement, ReactNode } from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/auth/AuthContext';
import { tokenStore } from '@/api/client';
import { Toaster } from '@/components/ui/sonner';

export function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0, staleTime: 0 },
      mutations: { retry: false },
    },
  });
}

interface Options {
  route?: string;
  authed?: boolean;
}

/** Renders `ui` inside QueryClient + Auth + Router. Seeds a token when authed. */
export function renderWithProviders(ui: ReactElement, opts: Options = {}) {
  const { route = '/', authed = false } = opts;
  if (authed) tokenStore.set('test-token');

  const client = createTestQueryClient();

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={client}>
        <AuthProvider>
          <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
          <Toaster />
        </AuthProvider>
      </QueryClientProvider>
    );
  }

  return { client, ...render(ui, { wrapper: Wrapper }) };
}
