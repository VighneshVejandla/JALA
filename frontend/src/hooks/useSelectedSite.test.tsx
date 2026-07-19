import { describe, expect, it } from 'vitest';
import { act, renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/server-ref';
import * as fx from '@/test/fixtures';
import { createTestQueryClient } from '@/test/utils';
import { useSelectedSite } from './useSelectedSite';

const BASE = '*/api/v1';
function ok<T>(data: T) {
  return HttpResponse.json({ success: true, message: 'ok', data, timestamp: '' });
}

function wrapper() {
  const client = createTestQueryClient();
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
}

describe('useSelectedSite', () => {
  it('defaults to the first site once loaded, then persists a selection', async () => {
    const { result } = renderHook(() => useSelectedSite(), { wrapper: wrapper() });

    await waitFor(() => expect(result.current.siteId).toBe('site-1'));
    expect(result.current.selectedSite?.siteCode).toBe('S-001');

    act(() => result.current.select('site-2'));
    expect(result.current.siteId).toBe('site-2');
    expect(localStorage.getItem('jala.selectedSiteId')).toBe('site-2');
  });

  it('resets a stale persisted site id that no longer exists', async () => {
    localStorage.setItem('jala.selectedSiteId', 'gone');
    server.use(http.get(`${BASE}/sites`, () => ok(fx.sites)));

    const { result } = renderHook(() => useSelectedSite(), { wrapper: wrapper() });
    await waitFor(() => expect(result.current.siteId).toBe('site-1'));
  });
});
