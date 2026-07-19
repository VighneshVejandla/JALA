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
function asAdmin() {
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));
}

describe('AdminDashboard', () => {
  it('renders the site overview and dashboard stats', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    expect(await screen.findByText('Active Cycles')).toBeInTheDocument();
    expect(await screen.findByText("Today's Harvest")).toBeInTheDocument();
  });
});

describe('SitesPage', () => {
  it('lists sites', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/sites', authed: true });
    expect(await screen.findByText('North Farm')).toBeInTheDocument();
    expect(screen.getByText('South Farm')).toBeInTheDocument();
  });

  it('shows an error block on failure', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/sites`, () =>
        HttpResponse.json(
          { success: false, message: 'x', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/sites', authed: true });
    expect(await screen.findByText(/could not load sites/i)).toBeInTheDocument();
  });
});

describe('UsersPage', () => {
  it('lists users with role labels', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    expect(await screen.findByText('Ada Admin')).toBeInTheDocument();
    expect(screen.getByText('Delivery Driver')).toBeInTheDocument();
  });
});
