import { describe, expect, it } from 'vitest';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/server-ref';
import * as fx from '@/test/fixtures';
import { renderWithProviders } from '@/test/utils';
import { AppRoutes } from '@/app/router';

const BASE = '*/api/v1';
function ok<T>(data: T) {
  return HttpResponse.json({ success: true, message: 'ok', data, timestamp: '' });
}
const asAdmin = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));

describe('feed & medicine cancel (admin)', () => {
  it('cancels a feed entry', async () => {
    asAdmin();
    let cancelled = false;
    server.use(
      http.patch(`${BASE}/feed-entries/:id/cancel`, () => {
        cancelled = true;
        return ok(fx.feedEntries[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/ponds/pond-1', authed: true });
    const user = userEvent.setup();
    await user.click(
      await screen.findByRole('button', { name: /cancel feed entry/i }),
    );
    await user.click(await screen.findByRole('button', { name: /cancel entry/i }));
    await waitFor(() => expect(cancelled).toBe(true));
  });

  it('cancels a medicine record', async () => {
    asAdmin();
    let cancelled = false;
    server.use(
      http.patch(`${BASE}/medicines/:id/cancel`, () => {
        cancelled = true;
        return ok(fx.medicines[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/ponds/pond-1', authed: true });
    const user = userEvent.setup();
    await user.click(
      await screen.findByRole('button', { name: /cancel medicine/i }),
    );
    await user.click(await screen.findByRole('button', { name: /cancel record/i }));
    await waitFor(() => expect(cancelled).toBe(true));
  });
});

describe('Harvested pagination', () => {
  it('reveals more harvests with Show more', async () => {
    asAdmin();
    const many = Array.from({ length: 12 }, (_, i) => ({
      ...fx.harvestHistory[0],
      harvestId: `h-${i}`,
    }));
    server.use(
      http.get(`${BASE}/history/pond/:pondId/harvests`, () => ok(many)),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/harvested', authed: true });
    const user = userEvent.setup();
    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
    const more = await screen.findByRole('button', { name: /show more/i });
    await user.click(more);
    // After revealing, the button is gone (12 ≤ 20 shown).
    await waitFor(() =>
      expect(
        screen.queryByRole('button', { name: /show more/i }),
      ).not.toBeInTheDocument(),
    );
  });
});

describe('history restore (cancelled feed/medicine)', () => {
  it('restores a cancelled feed entry', async () => {
    asAdmin();
    let restored = false;
    server.use(
      http.get(`${BASE}/history/pond/:pondId/feeds`, () =>
        ok([
          {
            feedEntryId: 'fe-x',
            cycleNumber: 3,
            sessionNumber: 1,
            feedDate: '2026-07-10',
            feedSize: 'ONE',
            feedQuantityKg: 12,
            remarks: null,
            status: 'CANCELLED',
            createdBy: 'EMP-1',
          },
        ]),
      ),
      http.patch(`${BASE}/feed-entries/:id/restore`, () => {
        restored = true;
        return ok(fx.feedEntries[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/history', authed: true });
    const user = userEvent.setup();
    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
    await user.click(await screen.findByRole('tab', { name: /feeds/i }));
    await user.click(await screen.findByRole('button', { name: /restore/i }));
    await waitFor(() => expect(restored).toBe(true));
  });

  it('restores a cancelled medicine record', async () => {
    asAdmin();
    let restored = false;
    server.use(
      http.get(`${BASE}/history/pond/:pondId/medicines`, () =>
        ok([
          {
            medicineId: 'med-x',
            cycleNumber: 3,
            quantity: 5,
            unit: 'ML',
            remarks: null,
            status: 'CANCELLED',
            createdBy: 'EMP-1',
            createdAt: '2026-07-10T09:00:00',
            photos: [],
          },
        ]),
      ),
      http.patch(`${BASE}/medicines/:id/restore`, () => {
        restored = true;
        return ok(fx.medicines[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/history', authed: true });
    const user = userEvent.setup();
    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
    await user.click(await screen.findByRole('tab', { name: /medicine/i }));
    await user.click(await screen.findByRole('button', { name: /restore/i }));
    await waitFor(() => expect(restored).toBe(true));
  });
});

describe('harvested bill link', () => {
  it('shows a View bill link when a bill photo exists', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/harvested', authed: true });
    const user = userEvent.setup();
    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
    expect(await screen.findByRole('link', { name: /view bill/i })).toBeInTheDocument();
  });
});

describe('skeletons', () => {
  it('shows a skeleton placeholder while sites load', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/sites`, async () => {
        await new Promise((r) => setTimeout(r, 50));
        return ok(fx.sites);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/sites', authed: true });
    expect(await screen.findByLabelText('Loading')).toBeInTheDocument();
    expect(await screen.findByText('North Farm')).toBeInTheDocument();
  });
});
