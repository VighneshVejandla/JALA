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
function err(status = 500) {
  return HttpResponse.json(
    { success: false, message: 'boom', data: null, timestamp: '' },
    { status },
  );
}
const asAdmin = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));

describe('AnalyticsPage', () => {
  it('renders KPI cards and the 12-month charts', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/analytics', authed: true });

    expect(await screen.findByText('Feed (month)')).toBeInTheDocument();
    expect(await screen.findByText('Revenue (month)')).toBeInTheDocument();
    expect(await screen.findByText(/revenue · last 12 months/i)).toBeInTheDocument();
    expect(await screen.findByText(/harvest · last 12 months/i)).toBeInTheDocument();
  });
});

describe('InventoryPage', () => {
  it('lists site inventory and flags low stock', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/inventory', authed: true });

    expect(await screen.findByText('North Farm')).toBeInTheDocument();
    expect(await screen.findByText('South Farm')).toBeInTheDocument();
    // South Farm has <20% remaining → low stock badge
    expect(await screen.findByText('Low stock')).toBeInTheDocument();
  });

  it('shows an empty state', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/feed-inventory`, () => ok([])));
    renderWithProviders(<AppRoutes />, { route: '/admin/inventory', authed: true });
    expect(await screen.findByText('No inventory')).toBeInTheDocument();
  });

  it('shows an error state', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/feed-inventory`, () => err()));
    renderWithProviders(<AppRoutes />, { route: '/admin/inventory', authed: true });
    expect(
      await screen.findByText(/could not load inventory/i),
    ).toBeInTheDocument();
  });
});

describe('AdminDashboard quick links', () => {
  it('navigates to Analytics from a dashboard tile', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    const user = userEvent.setup();
    // The dashboard tile is a link labelled "Analytics".
    const tile = (await screen.findAllByText('Analytics'))[0];
    await user.click(tile);
    expect(await screen.findByText('Feed (month)')).toBeInTheDocument();
  });
});
