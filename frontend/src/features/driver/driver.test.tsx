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
function asDriver() {
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.driverUser)));
}

describe('DriverDeliveries', () => {
  it('lists deliveries', async () => {
    asDriver();
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    expect(await screen.findByText('Morning run')).toBeInTheDocument();
    expect(screen.getByText('IN_PROGRESS')).toBeInTheDocument();
  });

  it('shows an empty state with no deliveries', async () => {
    asDriver();
    server.use(http.get(`${BASE}/feed-deliveries`, () => ok([])));
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    expect(await screen.findByText(/no deliveries yet/i)).toBeInTheDocument();
  });

  it('creates a delivery from the dialog', async () => {
    asDriver();
    let created = false;
    server.use(
      http.post(`${BASE}/feed-deliveries`, () => {
        created = true;
        return ok({ ...fx.deliveries[0], id: 'd-new' });
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /new/i }));
    await user.click(await screen.findByRole('button', { name: /start delivery/i }));

    await waitFor(() => expect(created).toBe(true));
  });

  it('surfaces an error toast when creation fails', async () => {
    asDriver();
    server.use(
      http.post(`${BASE}/feed-deliveries`, () =>
        HttpResponse.json(
          { success: false, message: 'Cannot create', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /new/i }));
    await user.click(await screen.findByRole('button', { name: /start delivery/i }));

    expect(await screen.findByText('Cannot create')).toBeInTheDocument();
  });
});
