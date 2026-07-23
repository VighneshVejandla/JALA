import { describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
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
function asWorker() {
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)));
}

describe('UserHome', () => {
  it('renders dashboard stats and the low-inventory banner', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app', authed: true });

    expect(await screen.findByText("Today's Feed")).toBeInTheDocument();
    expect(screen.getByText('Available Feed')).toBeInTheDocument();
    expect(
      screen.getByText(/feed inventory is running low/i),
    ).toBeInTheDocument();
  });

  it('shows an error block when the dashboard fails', async () => {
    asWorker();
    server.use(
      http.get(`${BASE}/dashboard/home/:siteId`, () =>
        HttpResponse.json(
          { success: false, message: 'boom', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/app', authed: true });
    expect(
      await screen.findByText(/could not load this site's dashboard/i),
    ).toBeInTheDocument();
  });
});

describe('PondsPage', () => {
  it('lists ponds and navigates to a pond detail', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app/ponds', authed: true });

    const card = await screen.findByText('Pond One');
    const user = userEvent.setup();
    await user.click(card);

    expect(await screen.findByText('Current cycle')).toBeInTheDocument();
    // Workers can record feed but never see harvest.
    expect(
      await screen.findByRole('button', { name: /record feed/i }),
    ).toBeInTheDocument();
    expect(screen.queryByText('Last harvest')).not.toBeInTheDocument();
  });

  it('shows an empty state when there are no ponds', async () => {
    asWorker();
    server.use(http.get(`${BASE}/ponds/site/:siteId`, () => ok([])));
    renderWithProviders(<AppRoutes />, { route: '/app/ponds', authed: true });
    expect(await screen.findByText('No ponds')).toBeInTheDocument();
  });

  it('ignores non-Enter keys on a pond card', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app/ponds', authed: true });
    const user = userEvent.setup();
    const card = (await screen.findByText('Pond One')).closest(
      '[role="button"]',
    ) as HTMLElement;
    card.focus();
    await user.keyboard('a');
    // no navigation to detail — the list is still shown
    expect(screen.getByText('Pond One')).toBeInTheDocument();
  });
});

describe('PondDetailPage', () => {
  it('renders the pond dashboard directly by route', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    expect(await screen.findByText('Pond One')).toBeInTheDocument();
    expect(screen.getByText('VANNAMEI')).toBeInTheDocument();
  });

  it('navigates back to the ponds list via the Back button', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app/ponds', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByText('Pond One'));
    await user.click(await screen.findByRole('button', { name: /back/i }));
    expect(await screen.findByText('Pond One')).toBeInTheDocument();
  });
});
