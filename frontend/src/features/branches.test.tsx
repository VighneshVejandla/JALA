import { describe, expect, it } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/server-ref';
import * as fx from '@/test/fixtures';
import { renderWithProviders } from '@/test/utils';
import { AppRoutes } from '@/app/router';
import { ProfilePage } from '@/features/shared/ProfilePage';

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
const asWorker = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)));
const asDriver = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.driverUser)));
const asAdmin = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));

describe('healthy (non-warning) dashboards', () => {
  it('UserHome without low inventory hides the banner', async () => {
    asWorker();
    server.use(
      http.get(`${BASE}/dashboard/home/:id`, () => ok(fx.homeDashboardHealthy)),
    );
    renderWithProviders(<AppRoutes />, { route: '/app', authed: true });
    expect(await screen.findByText("Today's Feed")).toBeInTheDocument();
    expect(
      screen.queryByText(/feed inventory is running low/i),
    ).not.toBeInTheDocument();
  });

  it('AdminDashboard without low inventory hides the banner', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/dashboard/home/:id`, () => ok(fx.homeDashboardHealthy)),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    expect(await screen.findByText("Today's Harvest")).toBeInTheDocument();
    expect(screen.queryByText(/feed inventory is low/i)).not.toBeInTheDocument();
  });
});

describe('pond detail without harvests', () => {
  it('hides the last-harvest card', async () => {
    asWorker();
    server.use(
      http.get(`${BASE}/dashboard/:pondId`, () => ok(fx.pondDashboardNoHarvest)),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    expect(await screen.findByText('Current cycle')).toBeInTheDocument();
    expect(screen.queryByText('Last harvest')).not.toBeInTheDocument();
  });

  it('shows an error block when the pond fails to load', async () => {
    asWorker();
    server.use(http.get(`${BASE}/dashboard/:pondId`, () => err()));
    renderWithProviders(<AppRoutes />, {
      route: '/app/ponds/pond-1',
      authed: true,
    });
    expect(
      await screen.findByText(/could not load this pond/i),
    ).toBeInTheDocument();
  });
});

describe('ponds page keyboard navigation and errors', () => {
  it('navigates to detail via the Enter key', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app/ponds', authed: true });
    const card = await screen.findByText('Pond One');
    (card.closest('[role="button"]') as HTMLElement | null)?.focus();
    await userEvent.setup().keyboard('{Enter}');
    expect(await screen.findByText('Current cycle')).toBeInTheDocument();
  });

  it('shows an error block when ponds fail to load', async () => {
    asWorker();
    server.use(http.get(`${BASE}/ponds/site/:id`, () => err()));
    renderWithProviders(<AppRoutes />, { route: '/app/ponds', authed: true });
    expect(await screen.findByText(/could not load ponds/i)).toBeInTheDocument();
  });
});

describe('driver delivery status tones and errors', () => {
  it('renders every status tone and null fields', async () => {
    asDriver();
    server.use(
      http.get(`${BASE}/feed-deliveries`, () => ok(fx.deliveriesAllStatuses)),
    );
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    expect(await screen.findByText('COMPLETED')).toBeInTheDocument();
    expect(screen.getByText('CANCELLED')).toBeInTheDocument();
    expect(screen.getByText('PENDING')).toBeInTheDocument();
  });

  it('shows an error block when deliveries fail to load', async () => {
    asDriver();
    server.use(http.get(`${BASE}/feed-deliveries`, () => err()));
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    expect(
      await screen.findByText(/could not load deliveries/i),
    ).toBeInTheDocument();
  });
});

describe('users list branches', () => {
  it('renders an inactive user without email', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/users`, () => ok([fx.adminUser, fx.inactiveUser])),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    expect(await screen.findByText('Ines Inactive')).toBeInTheDocument();
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });
});

describe('alerts error state', () => {
  it('shows an error block', async () => {
    asWorker();
    server.use(http.get(`${BASE}/notifications`, () => err()));
    renderWithProviders(<AppRoutes />, { route: '/app/alerts', authed: true });
    expect(await screen.findByText(/could not load alerts/i)).toBeInTheDocument();
  });
});

describe('NotFound navigation', () => {
  it('navigates home when unauthenticated', async () => {
    renderWithProviders(<AppRoutes />, { route: '/definitely-missing' });
    const btn = await screen.findByRole('button', { name: /go home/i });
    await userEvent.setup().click(btn);
    expect(await screen.findByText('JALA Aqua Management')).toBeInTheDocument();
  });
});

describe('ProfilePage guard', () => {
  it('renders nothing without a signed-in user', async () => {
    renderWithProviders(<ProfilePage />, { route: '/', authed: false });
    // Unauthenticated: the profile UI (its sign-out button) never appears.
    await waitFor(() =>
      expect(
        screen.queryByRole('button', { name: /sign out/i }),
      ).not.toBeInTheDocument(),
    );
  });
});
