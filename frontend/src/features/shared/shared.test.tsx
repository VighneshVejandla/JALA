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
function asWorker() {
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)));
}

describe('AlertsPage', () => {
  it('lists notifications and marks one as read', async () => {
    asWorker();
    let marked = false;
    server.use(
      http.patch(`${BASE}/notifications/:id/read`, () => {
        marked = true;
        return ok({ ...fx.notifications[0], status: 'READ' });
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/app/alerts', authed: true });

    expect(await screen.findByText('Low feed')).toBeInTheDocument();
    const user = userEvent.setup();
    await user.click(
      await screen.findByRole('button', { name: /mark as read/i }),
    );
    await waitFor(() => expect(marked).toBe(true));
  });

  it('shows an empty state', async () => {
    asWorker();
    server.use(http.get(`${BASE}/notifications`, () => ok([])));
    renderWithProviders(<AppRoutes />, { route: '/app/alerts', authed: true });
    expect(await screen.findByText('No alerts')).toBeInTheDocument();
  });
});

describe('ProfilePage', () => {
  it('shows the signed-in user details', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app/profile', authed: true });
    expect(await screen.findByText('Will Worker')).toBeInTheDocument();
    expect(screen.getByText('Field Worker')).toBeInTheDocument();
  });
});
