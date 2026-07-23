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
const asDriver = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.driverUser)));

describe('ReportsPage', () => {
  it('renders filters and downloads a report', async () => {
    asAdmin();
    let exported = false;
    server.use(
      http.post(`${BASE}/export/:kind/:format`, () => {
        exported = true;
        return HttpResponse.arrayBuffer(new ArrayBuffer(8), {
          headers: { 'Content-Type': 'application/pdf' },
        });
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/reports', authed: true });
    expect(await screen.findByText('Download reports')).toBeInTheDocument();
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /excel/i }))[0]);
    await waitFor(() => expect(exported).toBe(true));
  });
});

describe('SearchPage', () => {
  it('searches and shows grouped results incl. users', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/search', authed: true });
    const user = userEvent.setup();
    await user.type(await screen.findByPlaceholderText(/search sites/i), 'north');
    expect(await screen.findByText('North Farm')).toBeInTheDocument();
    expect(screen.getByText('Pond One')).toBeInTheDocument();
    // User results group
    expect(screen.getByText('Ada Admin')).toBeInTheDocument();
  });

  it('ReportsPage shows a no-sites state', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/sites`, () => ok([])));
    renderWithProviders(<AppRoutes />, { route: '/admin/reports', authed: true });
    expect(await screen.findByText('No sites')).toBeInTheDocument();
  });

  it('shows a no-match state', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/search`, () =>
        ok({
          sites: [],
          ponds: [],
          feedEntries: [],
          medicineEntries: [],
          harvests: [],
          notifications: [],
        }),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/search', authed: true });
    const user = userEvent.setup();
    await user.type(await screen.findByPlaceholderText(/search sites/i), 'zzz');
    expect(await screen.findByText('No matches')).toBeInTheDocument();
  });
});

describe('AdminDeliveriesPage', () => {
  it('lists deliveries, filters, and opens detail', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/deliveries', authed: true });
    const user = userEvent.setup();
    expect(await screen.findByText('Morning run')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /cancelled/i }));
    await user.click(screen.getByRole('button', { name: 'All' }));
    await user.click(await screen.findByText('Morning run'));
    expect(await screen.findByText('Drop-offs')).toBeInTheDocument();
  });
});

describe('AdminDeliveriesPage cancelled + keyboard', () => {
  it('renders a cancelled delivery and ignores non-Enter keys', async () => {
    asAdmin();
    server.use(
      http.get(`${BASE}/feed-deliveries`, () =>
        ok([{ ...fx.deliveries[0], status: 'CANCELLED' }]),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/deliveries', authed: true });
    const user = userEvent.setup();
    expect(await screen.findAllByText('CANCELLED')).not.toHaveLength(0);
    const card = (await screen.findByText('Morning run')).closest(
      '[role="button"]',
    ) as HTMLElement;
    card.focus();
    await user.keyboard('a');
    expect(screen.getByText('Morning run')).toBeInTheDocument();
  });
});

describe('UsersPage edit + sites', () => {
  it('edits a user', async () => {
    asAdmin();
    let updated = false;
    server.use(
      http.patch(`${BASE}/users/:id`, () => {
        updated = true;
        return ok(fx.workerUser);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /edit/i }))[0]);
    const dialog = await screen.findByRole('dialog');
    const name = within(dialog).getByLabelText(/full name/i);
    await user.clear(name);
    await user.type(name, 'Renamed User');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    await waitFor(() => expect(updated).toBe(true));
  });

  it('assigns a site to a user', async () => {
    asAdmin();
    let toggled = false;
    server.use(
      http.delete(`${BASE}/users/:id/sites/:siteId`, () => {
        toggled = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /manage sites/i }))[0]);
    const dialog = await screen.findByRole('dialog');
    // site-1 is assigned in the fixture → toggling it off calls DELETE
    await user.click(within(dialog).getByRole('switch', { name: /toggle north farm/i }));
    await waitFor(() => expect(toggled).toBe(true));
  });

  it('assigns a previously-unassigned site (POST)', async () => {
    asAdmin();
    let posted = false;
    server.use(
      http.post(`${BASE}/users/:id/sites/:siteId`, () => {
        posted = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/admin/users', authed: true });
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /manage sites/i }))[0]);
    const dialog = await screen.findByRole('dialog');
    // South Farm (site-2) is NOT assigned → toggling it on calls POST.
    await user.click(within(dialog).getByRole('switch', { name: /toggle south farm/i }));
    await waitFor(() => expect(posted).toBe(true));
  });
});

describe('Alerts filters + mark all read', () => {
  it('filters unread and marks all read', async () => {
    asWorker();
    let marked = 0;
    server.use(
      http.patch(`${BASE}/notifications/:id/read`, () => {
        marked += 1;
        return ok({ ...fx.notifications[0], status: 'READ' });
      }),
    );
    renderWithProviders(<AppRoutes />, { route: '/app/alerts', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /^unread/i }));
    await user.click(screen.getByRole('button', { name: /mark all read/i }));
    await waitFor(() => expect(marked).toBeGreaterThanOrEqual(1));
  });

  it('filters by category', async () => {
    asWorker();
    renderWithProviders(<AppRoutes />, { route: '/app/alerts', authed: true });
    const user = userEvent.setup();
    // Two fixture notifications have distinct types → the category select shows.
    expect(await screen.findByText('Low feed')).toBeInTheDocument();
    const combo = await screen.findByRole('combobox');
    await user.click(combo);
    await user.click(await screen.findByRole('option', { name: /low inventory/i }));
    // The INFO notification ("Cycle started") is filtered out.
    expect(screen.queryByText('Cycle started')).not.toBeInTheDocument();
  });
});

describe('TopBar notification badge', () => {
  it('shows the unread count and navigates to alerts', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin', authed: true });
    const user = userEvent.setup();
    const bell = await screen.findByRole('button', { name: /alerts/i });
    await user.click(bell);
    expect(await screen.findByText('Low feed')).toBeInTheDocument();
  });
});

describe('Medicine photo gallery + feed edit', () => {
  it('views medicine photos', async () => {
    asAdmin();
    server.use(http.get(`${BASE}/medicines`, () => ok(fx.medicines)));
    renderWithProviders(<AppRoutes />, {
      route: '/admin/ponds/pond-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click((await screen.findAllByRole('button', { name: /photos/i }))[0]);
    expect(await screen.findByText('Medicine photos')).toBeInTheDocument();
  });

  it('edits a feed entry', async () => {
    asAdmin();
    let updated = false;
    server.use(
      http.patch(`${BASE}/feed-entries/:id`, () => {
        updated = true;
        return ok(fx.feedEntries[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/admin/ponds/pond-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(
      await screen.findByRole('button', { name: /edit feed entry/i }),
    );
    const dialog = await screen.findByRole('dialog');
    const qty = within(dialog).getByLabelText(/quantity/i);
    await user.clear(qty);
    await user.type(qty, '18');
    await user.click(within(dialog).getByRole('button', { name: /save changes/i }));
    await waitFor(() => expect(updated).toBe(true));
  });
});

describe('Driver deliveries filter', () => {
  it('filters by status', async () => {
    asDriver();
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    const user = userEvent.setup();
    expect(await screen.findByText('Morning run')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /cancelled/i }));
    // The only fixture delivery is ACTIVE, so the CANCELLED filter hides it.
    expect(screen.queryByText('Morning run')).not.toBeInTheDocument();
  });
});
