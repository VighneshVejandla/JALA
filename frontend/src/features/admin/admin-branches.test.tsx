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
function err(status = 500) {
  return HttpResponse.json(
    { success: false, message: 'boom', data: null, timestamp: '' },
    { status },
  );
}
const asAdmin = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));

describe('SiteDetail branches', () => {
  it('shows an error when the site fails to load', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/sites/:id`, () => err()));
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    expect(
      await screen.findByText(/could not load this site/i),
    ).toBeInTheDocument();
  });

  it('renders a site with null location/acres and an inactive badge', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/sites/:id`, () => ok(fx.sites[1])), // South Farm: nulls, inactive
      http.get(`${BASE}/ponds/site/:id`, () =>
        ok([{ ...fx.ponds[0], id: 'pond-x', pondAcres: null }]),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-2',
      authed: true,
    });
    expect(await screen.findByText('South Farm')).toBeInTheDocument();
    expect(await screen.findByText('Inactive')).toBeInTheDocument();
  });

  it('shows an empty ponds state', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/ponds/site/:id`, () => ok([])),
      http.get(`${BASE}/analytics/inventory/site/:siteId`, () => err()),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    expect(await screen.findByText('No ponds')).toBeInTheDocument();
  });
});

describe('Pond sections — empty states', () => {
  it('renders empty feed / medicine / harvest sections', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/analytics/feed/pond/:pondId`, () => err()),
      http.get(`${BASE}/feed-schedules/cycle/:cycleId`, () => ok([])),
      http.get(`${BASE}/feed-entries`, () => ok([])),
      http.get(`${BASE}/medicines`, () => ok([])),
      http.get(`${BASE}/harvests`, () => ok([])),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/ponds/pond-1',
      authed: true,
    });
    expect(
      await screen.findByText(/no feeding sessions yet/i),
    ).toBeInTheDocument();
    expect(
      await screen.findByText(/no medicine recorded/i),
    ).toBeInTheDocument();
    expect(
      await screen.findByText(/no harvests recorded/i),
    ).toBeInTheDocument();
  });

  it('adds and removes a feeding time in the sessions dialog', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, {
      route: '/admin/ponds/pond-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /sessions/i }));
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: /add time/i }));
    // a second time row now has a remove button
    await user.click(
      within(dialog).getByRole('button', { name: /remove time 2/i }),
    );
    expect(dialog).toBeInTheDocument();
  });
});

describe('site/pond activation toggles', () => {
  it('deactivates a site and a pond from the site detail', async () => {
    asAdmin();
    let siteToggled = false;
    let pondToggled = false;
    server.use(
      http.patch(`${BASE}/sites/:id/deactivate`, () => {
        siteToggled = true;
        return ok(null);
      }),
      http.patch(`${BASE}/ponds/:id/deactivate`, () => {
        pondToggled = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('switch', { name: /deactivate site/i }));
    await waitFor(() => expect(siteToggled).toBe(true));
    await user.click(
      await screen.findByRole('switch', { name: /deactivate pond one/i }),
    );
    await waitFor(() => expect(pondToggled).toBe(true));
  });
});

describe('activation toggles the other direction', () => {
  it('activates an inactive site and pond', async () => {
    asAdmin();
    let siteActivated = false;
    let pondActivated = false;
    server.use(
      http.get(`${BASE}/sites/:id`, () => ok(fx.sites[1])), // inactive site
      http.get(`${BASE}/ponds/site/:id`, () =>
        ok([{ ...fx.ponds[0], isActive: false }]),
      ),
      http.patch(`${BASE}/sites/:id/activate`, () => {
        siteActivated = true;
        return ok(null);
      }),
      http.patch(`${BASE}/ponds/:id/activate`, () => {
        pondActivated = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-2',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('switch', { name: /activate site/i }));
    await waitFor(() => expect(siteActivated).toBe(true));
    await user.click(
      await screen.findByRole('switch', { name: /activate pond one/i }),
    );
    await waitFor(() => expect(pondActivated).toBe(true));
  });
});

describe('extra small branches', () => {
  it('renders a medicine without remarks', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/medicines`, () =>
        ok([
          fx.medicines[0],
          { ...fx.medicines[0], id: 'med-2', remarks: null },
        ]),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/ponds/pond-1',
      authed: true,
    });
    // two medicine rows render (one with, one without remarks)
    expect(await screen.findByText('Vitamin C', { exact: false })).toBeInTheDocument();
  });

  it('surfaces site and pond toggle errors', async () => {
    asAdmin();
    server.use(
      http.patch(`${BASE}/sites/:id/deactivate`, () =>
        HttpResponse.json(
          { success: false, message: 'Site toggle failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
      http.patch(`${BASE}/ponds/:id/deactivate`, () =>
        HttpResponse.json(
          { success: false, message: 'Pond toggle failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('switch', { name: /deactivate site/i }));
    expect(await screen.findByText('Site toggle failed')).toBeInTheDocument();
    await user.click(
      await screen.findByRole('switch', { name: /deactivate pond one/i }),
    );
    expect(await screen.findByText('Pond toggle failed')).toBeInTheDocument();
  });
});

describe('UsersPage add-user error', () => {
  it('surfaces a create-user failure', async () => {
    asAdmin();
    server.use(
      http.post(`${BASE}/users`, () =>
        HttpResponse.json(
          { success: false, message: 'Code taken', data: null, timestamp: '' },
          { status: 409 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /add user/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/full name/i), 'X');
    await user.type(within(dialog).getByLabelText(/employee code/i), 'E1');
    await user.click(within(dialog).getByRole('combobox'));
    await user.click(await screen.findByRole('option', { name: /WORKER/ }));
    await user.type(within(dialog).getByLabelText(/temporary password/i), 'longpassword1');
    await user.click(within(dialog).getByRole('button', { name: /create user/i }));
    expect(await screen.findByText('Code taken')).toBeInTheDocument();
  });
});

describe('card keyboard handlers ignore non-Enter keys', () => {
  it('SiteDetail pond card does not navigate on a non-Enter key', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    const user = userEvent.setup();
    const card = (await screen.findByText('Pond One')).closest(
      '[role="button"]',
    ) as HTMLElement;
    card.focus();
    await user.keyboard('a');
    // still on the site detail (Ponds heading present), no navigation
    expect(screen.getByText('Ponds')).toBeInTheDocument();
  });
});

describe('HistoryPage branches', () => {
  it('prompts to select a pond, then shows medicine and harvest tabs', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/history', authed: true });
    const user = userEvent.setup();

    expect(
      await screen.findByText(/pick a pond to view its cycle/i),
    ).toBeInTheDocument();

    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));

    await user.click(await screen.findByRole('tab', { name: /medicine/i }));
    expect(await screen.findByText(/photo\(s\)/i)).toBeInTheDocument();

    await user.click(screen.getByRole('tab', { name: /harvests/i }));
    expect(await screen.findAllByText(/250 kg/i)).not.toHaveLength(0);
  });
});

describe('create/toggle error paths', () => {
  it('SitesPage surfaces a create error', async () => {
    asAdmin();
    server.use(
      http.post(`${BASE}/sites`, () =>
        HttpResponse.json(
          { success: false, message: 'Site exists', data: null, timestamp: '' },
          { status: 409 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/sites', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /add site/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/site code/i), 'S-1');
    await user.type(within(dialog).getByLabelText(/site name/i), 'X');
    await user.type(within(dialog).getByLabelText(/owner name/i), 'Y');
    await user.type(within(dialog).getByLabelText(/location/i), 'Z');
    await user.type(within(dialog).getByLabelText(/total acres/i), '5');
    await user.click(within(dialog).getByRole('button', { name: /create site/i }));
    expect(await screen.findByText('Site exists')).toBeInTheDocument();
  });

  it('SiteDetail surfaces an add-pond error', async () => {
    asAdmin();
    server.use(
      http.post(`${BASE}/ponds`, () =>
        HttpResponse.json(
          { success: false, message: 'Pond exists', data: null, timestamp: '' },
          { status: 409 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/sites/site-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /add pond/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/pond code/i), 'P-9');
    await user.type(within(dialog).getByLabelText(/pond name/i), 'X');
    await user.type(within(dialog).getByLabelText(/pond acres/i), '2');
    await user.click(within(dialog).getByRole('button', { name: /create pond/i }));
    expect(await screen.findByText('Pond exists')).toBeInTheDocument();
  });

  it('UsersPage surfaces a toggle error', async () => {
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
    const switches = await screen.findAllByRole('switch');
    await user.click(switches[0]);
    expect(await screen.findByText('Toggle failed')).toBeInTheDocument();
  });
});

describe('HarvestedPage branches', () => {
  it('shows an empty harvest list', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/history/pond/:pondId/harvests`, () => ok([])));
    renderWithProviders(<AppRoutes />, {
      route: '/admin/harvested',
      authed: true,
    });
    const user = userEvent.setup();
    const combos = await screen.findAllByRole('combobox');
    await user.click(combos[combos.length - 1]);
    await user.click(await screen.findByRole('option', { name: /Pond One/i }));
    expect(await screen.findByText('No harvests')).toBeInTheDocument();
  });
});
