import { describe, expect, it } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/server-ref';
import * as fx from '@/test/fixtures';
import { renderWithProviders } from '@/test/utils';
import { AppRoutes } from './router';

const BASE = '*/api/v1';
function ok<T>(data: T) {
  return HttpResponse.json({ success: true, message: 'ok', data, timestamp: '' });
}
function meIs(user: unknown) {
  server.use(http.get(`${BASE}/auth/me`, () => ok(user)));
}

describe('unauthenticated access', () => {
  it('redirects the root path to the login screen', async () => {
    renderWithProviders(<AppRoutes />, { route: '/' });
    expect(await screen.findByText('JALA Aqua Management')).toBeInTheDocument();
  });

  it('redirects a protected route to login', async () => {
    renderWithProviders(<AppRoutes />, { route: '/app' });
    expect(await screen.findByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });
});

describe('login flow', () => {
  it('logs a worker in and lands on the user home', async () => {
    renderWithProviders(<AppRoutes />, { route: '/login' });
    const user = userEvent.setup();

    await user.type(await screen.findByLabelText(/employee code/i), 'EMP-WORK');
    await user.type(screen.getByLabelText(/password/i), 'secret');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText("Today's Feed")).toBeInTheDocument();
  });

  it('shows an error and stays on login for bad credentials', async () => {
    renderWithProviders(<AppRoutes />, { route: '/login' });
    const user = userEvent.setup();

    await user.type(await screen.findByLabelText(/employee code/i), 'BAD');
    await user.type(screen.getByLabelText(/password/i), 'x');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(
      await screen.findByText(/invalid employee code or password/i),
    ).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    renderWithProviders(<AppRoutes />, { route: '/login' });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /sign in/i }));
    expect(await screen.findByText(/employee code is required/i)).toBeInTheDocument();
  });

  it('redirects an already-authenticated user away from login', async () => {
    meIs(fx.workerUser);
    renderWithProviders(<AppRoutes />, { route: '/login', authed: true });
    expect(await screen.findByText("Today's Feed")).toBeInTheDocument();
  });

  it('returns the user to the page they first requested', async () => {
    // Hitting a protected page while logged out stores it as `from`;
    // logging in should land there rather than the role default.
    renderWithProviders(<AppRoutes />, { route: '/app/ponds' });
    const user = userEvent.setup();
    await user.type(await screen.findByLabelText(/employee code/i), 'EMP-WORK');
    await user.type(screen.getByLabelText(/password/i), 'secret');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    expect(await screen.findByText('Pond One')).toBeInTheDocument();
  });
});

describe('role-based routing', () => {
  it('sends an admin to the admin dashboard from root', async () => {
    meIs(fx.adminUser);
    renderWithProviders(<AppRoutes />, { route: '/', authed: true });
    expect(await screen.findByText('Active Cycles')).toBeInTheDocument();
  });

  it('sends a driver to deliveries from root', async () => {
    meIs(fx.driverUser);
    renderWithProviders(<AppRoutes />, { route: '/', authed: true });
    expect(await screen.findByText('My deliveries')).toBeInTheDocument();
  });

  it('blocks a worker from the admin area and redirects home', async () => {
    meIs(fx.workerUser);
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    expect(await screen.findByText("Today's Feed")).toBeInTheDocument();
  });
});

describe('not found', () => {
  it('shows the 404 screen for an unknown path', async () => {
    renderWithProviders(<AppRoutes />, { route: '/nope' });
    expect(await screen.findByText(/page not found/i)).toBeInTheDocument();
  });
});

describe('logout', () => {
  it('returns to the login screen after signing out', async () => {
    meIs(fx.workerUser);
    renderWithProviders(<AppRoutes />, { route: '/app/profile', authed: true });
    const user = userEvent.setup();
    // Two controls expose "sign out" (the top-bar icon and the profile button);
    // the profile page's button is the last one.
    const buttons = await screen.findAllByRole('button', { name: /sign out/i });
    await user.click(buttons[buttons.length - 1]);
    await waitFor(() =>
      expect(screen.getByText('JALA Aqua Management')).toBeInTheDocument(),
    );
  });
});
