import { describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
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

describe('data-variation branches', () => {
  it('PondManage treats a harvested cycle as inactive', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/pond-cycles/active/:pondId`, () =>
        ok({ ...fx.activeCycle, status: 'HARVESTED' }),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/ponds/pond-1',
      authed: true,
    });
    // No active cycle → start-cycle offered, recording sections hidden.
    expect(await screen.findByText(/no active cycle/i)).toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: /record harvest/i }),
    ).not.toBeInTheDocument();
  });

  it('HarvestedPage renders a record with null buyer/price/amount', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/history/pond/:pondId/harvests`, () =>
        ok([
          {
            ...fx.harvestHistory[0],
            buyerName: null,
            sellingPricePerKg: null,
            totalAmount: null,
          },
        ]),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/harvested',
      authed: true,
    });
    const combos = await screen.findAllByRole('combobox');
    const user = (await import('@testing-library/user-event')).default.setup();
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
    expect(await screen.findAllByText(/250 kg/i)).not.toHaveLength(0);
  });

  it('PondDetailPage (worker) shows the no-cycle message when none is active', async () => {
    server.use(
      http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)),
      http.get(`${BASE}/dashboard/:pondId`, () =>
        ok({
          ...fx.pondDashboard,
          harvestCount: 0,
          activeCycleId: null,
          cycleStatus: null,
        }),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    expect(
      await screen.findByText(/pond cycle has not started yet/i),
    ).toBeInTheDocument();
    expect(screen.queryByText('Last harvest')).not.toBeInTheDocument();
  });
});
