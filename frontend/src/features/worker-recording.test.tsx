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
const asWorker = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)));

describe('Worker pond recording', () => {
  it('exposes feed/medicine/harvest recording but not session management', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    // read-only dashboard still renders
    expect(await screen.findByText('Current cycle')).toBeInTheDocument();
    // recording actions are present
    expect(
      await screen.findByRole('button', { name: /record feed/i }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: /add medicine/i }),
    ).toBeInTheDocument();
    // workers cannot create feeding sessions
    expect(
      screen.queryByRole('button', { name: /^sessions$/i }),
    ).not.toBeInTheDocument();
  });

  it('hides recording when there is no active cycle', async () => {
    asWorker();
    server.use(
      http.get(`${BASE}/dashboard/:pondId`, () =>
        ok({ ...fx.pondDashboard, activeCycleId: null, cycleStatus: null }),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    expect(await screen.findByText('Pond One')).toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: /record feed/i }),
    ).not.toBeInTheDocument();
  });

  it('records a feed entry as a worker', async () => {
    asWorker();
    let recorded = false;
    server.use(
      http.post(`${BASE}/feed-entries`, () => {
        recorded = true;
        return ok(fx.feedEntries[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /record feed/i }));
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getAllByRole('combobox')[0]);
    await user.click(await screen.findByRole('option', { name: /session 1/i }));
    await user.type(within(dialog).getByLabelText(/quantity/i), '15');
    await user.click(within(dialog).getByRole('button', { name: /save feed entry/i }));
    await waitFor(() => expect(recorded).toBe(true));
  });
});
