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
const asAdmin = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));
const asWorker = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)));

describe('Harvested page — edit & cancel', () => {
  async function openPond(user: ReturnType<typeof userEvent.setup>) {
    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
  }

  it('edits a harvest', async () => {
    asAdmin();
    let updated = false;
    server.use(
      http.patch(`${BASE}/harvests/:id`, () => {
        updated = true;
        return ok(fx.harvests[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/harvested', authed: true });
    const user = userEvent.setup();
    await openPond(user);
    await user.click(await screen.findByRole('button', { name: /edit harvest/i }));
    const dialog = await screen.findByRole('dialog');
    const qty = within(dialog).getByLabelText(/quantity/i);
    await user.clear(qty);
    await user.type(qty, '275');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    await waitFor(() => expect(updated).toBe(true));
  });

  it('cancels a harvest', async () => {
    asAdmin();
    let cancelled = false;
    server.use(
      http.patch(`${BASE}/harvests/:id/cancel`, () => {
        cancelled = true;
        return ok(fx.harvests[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/harvested', authed: true });
    const user = userEvent.setup();
    await openPond(user);
    await user.click(await screen.findByRole('button', { name: /cancel harvest/i }));
    await user.click(await screen.findByRole('button', { name: /^cancel harvest$/i }));
    await waitFor(() => expect(cancelled).toBe(true));
  });
});

describe('Pond edit', () => {
  it('edits a pond', async () => {
    asAdmin();
    let updated = false;
    server.use(
      http.patch(`${BASE}/ponds/:id`, () => {
        updated = true;
        return ok(fx.ponds[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/ponds/pond-1', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /edit pond/i }));
    const dialog = await screen.findByRole('dialog');
    const name = within(dialog).getByLabelText(/pond name/i);
    await user.clear(name);
    await user.type(name, 'Renamed Pond');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    await waitFor(() => expect(updated).toBe(true));
  });
});

describe('error paths', () => {
  it('surfaces a pond-edit error', async () => {
    asAdmin();
    server.use(
      http.patch(`${BASE}/ponds/:id`, () =>
        HttpResponse.json(
          { success: false, message: 'Pond edit failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/ponds/pond-1', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /edit pond/i }));
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    expect(await screen.findByText('Pond edit failed')).toBeInTheDocument();
  });

  it('surfaces a reset-password error', async () => {
    asAdmin();
    server.use(
      http.patch(`${BASE}/users/:id/password`, () =>
        HttpResponse.json(
          { success: false, message: 'Reset failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /reset password/i }))[0]);
    const dialog = await screen.findByRole('dialog');
    await user.type(
      within(dialog).getByLabelText(/new temporary password/i),
      'temppass123456',
    );
    await user.click(within(dialog).getByRole('button', { name: /reset password/i }));
    expect(await screen.findByText('Reset failed')).toBeInTheDocument();
  });
});

describe('pond edit with null acres', () => {
  it('defaults acres to 0 when the pond has none', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/ponds/:id`, () =>
        ok({ ...fx.ponds[0], pondAcres: null }),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/ponds/pond-1', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /edit pond/i }));
    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByLabelText(/acres/i)).toHaveValue(0);
  });
});

describe('Profile edit + change password', () => {
  it('edits the profile', async () => {
    asWorker();
    let updated = false;
    server.use(
      http.patch(`${BASE}/auth/me`, () => {
        updated = true;
        return ok(fx.workerUser);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/app/profile', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /edit profile/i }));
    const dialog = await screen.findByRole('dialog');
    const name = within(dialog).getByLabelText(/full name/i);
    await user.clear(name);
    await user.type(name, 'Will W');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    await waitFor(() => expect(updated).toBe(true));
  });

  it('changes the password', async () => {
    asWorker();
    let changed = false;
    server.use(
      http.patch(`${BASE}/auth/password`, () => {
        changed = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/app/profile', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /change password/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/current password/i), 'oldpass123456');
    await user.type(within(dialog).getByLabelText(/^new password/i), 'newpassword123');
    await user.type(within(dialog).getByLabelText(/confirm/i), 'newpassword123');
    await user.click(within(dialog).getByRole('button', { name: /^change password$/i }));
    await waitFor(() => expect(changed).toBe(true));
  });
});

describe('users list states', () => {
  it('shows the error state', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/users`, () =>
        HttpResponse.json(
          { success: false, message: 'x', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    expect(await screen.findByText(/could not load users/i)).toBeInTheDocument();
  });

  it('shows the empty state', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/users`, () => ok([])));
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    expect(await screen.findByText('No users')).toBeInTheDocument();
  });

  it('surfaces a user activation error', async () => {
    asAdmin();
    server.use(
      http.patch(`${BASE}/users/:id/deactivate`, () =>
        HttpResponse.json(
          { success: false, message: 'Toggle failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click(
      (await screen.findAllByRole('switch', { name: /deactivate user/i }))[0],
    );
    expect(await screen.findByText('Toggle failed')).toBeInTheDocument();
  });
});

describe('top bar notification badge', () => {
  it('renders the alerts bell without a count when nothing is unread', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/notifications`, () =>
        ok({ unreadCount: 0, notifications: [] }),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    // Bell present, but its accessible name has no "unread" suffix.
    expect(await screen.findByRole('button', { name: /^alerts$/i })).toBeInTheDocument();
  });

  it('caps the unread badge at 9+', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/notifications`, () =>
        ok({ unreadCount: 15, notifications: [] }),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    expect(await screen.findByText('9+')).toBeInTheDocument();
  });
});

describe('site detail pond card keyboard', () => {
  it('ignores non-Enter keys on a pond row', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/sites/site-1', authed: true });
    const user = userEvent.setup();
    const card = (await screen.findByText('Pond One')).closest(
      '[role="button"]',
    ) as HTMLElement;
    card.focus();
    await user.keyboard('x');
    expect(screen.getByText('Pond One')).toBeInTheDocument();
  });
});

describe('Admin reset password', () => {
  it('resets a user password', async () => {
    asAdmin();
    let reset = false;
    server.use(
      http.patch(`${BASE}/users/:id/password`, () => {
        reset = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /reset password/i }))[0]);
    const dialog = await screen.findByRole('dialog');
    await user.type(
      within(dialog).getByLabelText(/new temporary password/i),
      'temppass123456',
    );
    await user.click(within(dialog).getByRole('button', { name: /reset password/i }));
    await waitFor(() => expect(reset).toBe(true));
  });
});
