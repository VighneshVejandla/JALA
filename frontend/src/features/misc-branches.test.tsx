import { describe, expect, it } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
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

describe('LoginPage branches', () => {
  it('toggles password visibility', async () => {
    renderWithProviders(<AppRoutes />, { route: '/login' });
    const user = userEvent.setup();
    const pwd = await screen.findByLabelText('Password');
    expect(pwd).toHaveAttribute('type', 'password');
    await user.click(screen.getByRole('button', { name: /show password/i }));
    expect(pwd).toHaveAttribute('type', 'text');
    await user.click(screen.getByRole('button', { name: /hide password/i }));
    expect(pwd).toHaveAttribute('type', 'password');
  });

  it('shows the server message on a non-401 login failure', async () => {
    server.use(
      http.post(`${BASE}/auth/login`, () =>
        HttpResponse.json(
          { success: false, message: 'Server down', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/login' });
    const user = userEvent.setup();
    await user.type(await screen.findByLabelText(/employee code/i), 'EMP-WORK');
    await user.type(screen.getByLabelText('Password'), 'secret');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    expect(await screen.findByText('Server down')).toBeInTheDocument();
  });
});

describe('NotFound branches', () => {
  it('sends an authenticated admin back to their dashboard', async () => {
    server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));
    renderWithProviders(<AppRoutes />, { route: '/admin/nope', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /go home/i }));
    expect(await screen.findByText('Total Sites')).toBeInTheDocument();
  });
});

describe('session restore — hard failure', () => {
  it('logs out when /auth/me fails twice', async () => {
    server.use(http.get(`${BASE}/auth/me`, () => HttpResponse.error()));
    renderWithProviders(<AppRoutes />, { route: '/app', authed: true });
    // Both attempts fail → session dropped → back to login.
    await waitFor(() =>
      expect(screen.getByText('JALA Aqua Management')).toBeInTheDocument(),
    );
  });
});
