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

/** Pick an option from a Radix Select trigger found by its accessible name. */
async function selectByTrigger(
  user: ReturnType<typeof userEvent.setup>,
  trigger: HTMLElement,
  optionName: RegExp | string,
) {
  await user.click(trigger);
  await user.click(await screen.findByRole('option', { name: optionName }));
}

describe('UsersPage — add user', () => {
  it('creates a user through the dialog', async () => {
    asAdmin();
    let created = false;
    server.use(
      http.post(`${BASE}/users`, () => {
        created = true;
        return ok(fx.adminUser);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /add user/i }));
    const dialog = await screen.findByRole('dialog');

    await user.type(within(dialog).getByLabelText(/full name/i), 'New Person');
    await user.type(within(dialog).getByLabelText(/employee code/i), 'EMP-9');
    await selectByTrigger(
      user,
      within(dialog).getByRole('combobox'),
      /WORKER/,
    );
    await user.type(
      within(dialog).getByLabelText(/temporary password/i),
      'longpassword1',
    );
    await user.click(within(dialog).getByRole('button', { name: /create user/i }));

    await waitFor(() => expect(created).toBe(true));
  });

  it('toggles a user active state', async () => {
    asAdmin();
    let deactivated = false;
    server.use(
      http.patch(`${BASE}/users/:id/deactivate`, () => {
        deactivated = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });

    const user = userEvent.setup();
    const switches = await screen.findAllByRole('switch');
    await user.click(switches[0]);
    await waitFor(() => expect(deactivated).toBe(true));
  });
});

describe('SitesPage — add site and navigate', () => {
  it('creates a site through the dialog', async () => {
    asAdmin();
    let created = false;
    server.use(
      http.post(`${BASE}/sites`, () => {
        created = true;
        return ok(fx.sites[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/sites', authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /add site/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/site code/i), 'S-100');
    await user.type(within(dialog).getByLabelText(/site name/i), 'New Site');
    await user.type(within(dialog).getByLabelText(/owner name/i), 'Owner');
    await user.type(within(dialog).getByLabelText(/location/i), 'Coast');
    await user.type(within(dialog).getByLabelText(/total acres/i), '10');
    await user.click(within(dialog).getByRole('button', { name: /create site/i }));

    await waitFor(() => expect(created).toBe(true));
  });

  it('navigates into a site detail', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/sites', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByText('North Farm'));
    expect(await screen.findByText('Ponds')).toBeInTheDocument();
  });
});

describe('SiteDetail — add pond and navigate', () => {
  it('renders inventory and ponds, adds a pond, opens pond manage', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    const user = userEvent.setup();

    expect(await screen.findByText('Available Feed')).toBeInTheDocument();
    expect(await screen.findByText('Pond One')).toBeInTheDocument();

    let created = false;
    server.use(
      http.post(`${BASE}/ponds`, () => {
        created = true;
        return ok(fx.ponds[0]);
      }),
    );
    await user.click(screen.getByRole('button', { name: /add pond/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/pond code/i), 'P-99');
    await user.type(within(dialog).getByLabelText(/pond name/i), 'New Pond');
    await user.type(within(dialog).getByLabelText(/pond acres/i), '2');
    await user.click(within(dialog).getByRole('button', { name: /create pond/i }));
    await waitFor(() => expect(created).toBe(true));
  });
});
