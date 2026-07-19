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
function err(status = 404) {
  return HttpResponse.json(
    { success: false, message: 'x', data: null, timestamp: '' },
    { status },
  );
}
const asAdmin = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));

const ROUTE = '/admin/ponds/pond-1';

describe('PondManage — active cycle workspace', () => {
  it('renders every section with an active cycle', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });

    expect(await screen.findByText('Current cycle')).toBeInTheDocument();
    expect(await screen.findByText('Feed')).toBeInTheDocument();
    // feed analytics (month) + medicine record + harvest record
    expect(await screen.findByText('720 kg')).toBeInTheDocument();
    expect(await screen.findByText('Vitamin C', { exact: false })).toBeInTheDocument();
    expect(await screen.findByText('Buyer Co', { exact: false })).toBeInTheDocument();
  });

  it('converts the cycle to harvested', async () => {
    asAdmin();
    let harvested = false;
    server.use(
      http.patch(`${BASE}/pond-cycles/:id/harvest`, () => {
        harvested = true;
        return ok(null);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(
      await screen.findByRole('button', { name: /convert to harvested/i }),
    );
    await user.click(await screen.findByRole('button', { name: /confirm/i }));
    await waitFor(() => expect(harvested).toBe(true));
  });

  it('records a feed amount for a session', async () => {
    asAdmin();
    let recorded = false;
    server.use(
      http.post(`${BASE}/feed-entries`, () => {
        recorded = true;
        return ok(fx.feedEntries[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /record feed/i }));
    const dialog = await screen.findByRole('dialog');
    const combos = within(dialog).getAllByRole('combobox');
    await user.click(combos[0]); // session
    await user.click(await screen.findByRole('option', { name: /session 1/i }));
    await user.type(within(dialog).getByLabelText(/quantity/i), '12');
    await user.click(within(dialog).getByRole('button', { name: /save feed entry/i }));
    await waitFor(() => expect(recorded).toBe(true));
  });

  it('adds feeding sessions', async () => {
    asAdmin();
    let added = false;
    server.use(
      http.post(`${BASE}/feed-schedules`, () => {
        added = true;
        return ok(fx.feedSchedules);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /sessions/i }));
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: /add sessions/i }));
    await waitFor(() => expect(added).toBe(true));
  });

  it('adds a medicine record', async () => {
    asAdmin();
    let added = false;
    server.use(
      http.post(`${BASE}/medicines`, () => {
        added = true;
        return ok(fx.medicines[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /add medicine/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/quantity/i), '5');
    await user.click(within(dialog).getByRole('button', { name: /save medicine/i }));
    await waitFor(() => expect(added).toBe(true));
  });

  it('uploads a medicine photo', async () => {
    asAdmin();
    let uploaded = false;
    server.use(
      http.post(`${BASE}/medicine-photos`, () => {
        uploaded = true;
        return ok({ id: 'mp-1' });
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    const fileInput = await screen.findByLabelText('Medicine photo');
    await user.upload(
      fileInput,
      new File(['x'], 'scan.png', { type: 'image/png' }),
    );
    await waitFor(() => expect(uploaded).toBe(true));
  });

  it('records a harvest with a bill photo', async () => {
    asAdmin();
    let recorded = false;
    server.use(
      http.post(`${BASE}/harvests`, () => {
        recorded = true;
        return ok(fx.harvests[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(
      await screen.findByRole('button', { name: /record harvest/i }),
    );
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/quantity/i), '300');
    await user.upload(
      within(dialog).getByLabelText(/bill photo/i),
      new File(['x'], 'bill.png', { type: 'image/png' }),
    );
    await user.click(within(dialog).getByRole('button', { name: /save harvest/i }));
    await waitFor(() => expect(recorded).toBe(true));
  });
});

describe('PondManage — extra branches', () => {
  it('records a harvest with all optional fields', async () => {
    asAdmin();
    let recorded = false;
    server.use(
      http.post(`${BASE}/harvests`, () => {
        recorded = true;
        return ok(fx.harvests[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /record harvest/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/quantity/i), '300');
    await user.type(within(dialog).getByLabelText(/buyer/i), 'Acme');
    await user.type(within(dialog).getByLabelText(/price per kg/i), '150');
    await user.type(within(dialog).getByLabelText(/vehicle number/i), 'AP01');
    await user.type(within(dialog).getByLabelText(/remarks/i), 'good batch');
    await user.upload(
      within(dialog).getByLabelText(/bill photo/i),
      new File(['x'], 'bill.png', { type: 'image/png' }),
    );
    await user.click(within(dialog).getByRole('button', { name: /save harvest/i }));
    await waitFor(() => expect(recorded).toBe(true));
  });

  it('requires a bill photo for a harvest', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /record harvest/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/quantity/i), '300');
    await user.click(within(dialog).getByRole('button', { name: /save harvest/i }));
    expect(await screen.findByText(/bill photo is required/i)).toBeInTheDocument();
  });

  it('rejects empty feeding times', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /sessions/i }));
    const dialog = await screen.findByRole('dialog');
    await user.clear(within(dialog).getByLabelText(/feeding time 1/i));
    await user.click(within(dialog).getByRole('button', { name: /add sessions/i }));
    expect(
      await screen.findByText(/add at least one feeding time/i),
    ).toBeInTheDocument();
  });

  it('surfaces a medicine save error', async () => {
    asAdmin();
    server.use(
      http.post(`${BASE}/medicines`, () =>
        HttpResponse.json(
          { success: false, message: 'Medicine failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /add medicine/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/quantity/i), '5');
    await user.click(within(dialog).getByRole('button', { name: /save medicine/i }));
    expect(await screen.findByText('Medicine failed')).toBeInTheDocument();
  });
});

describe('PondManage — no active cycle', () => {
  it('shows start-cycle and creates a new cycle', async () => {
    asAdmin();
    let created = false;
    server.use(
      http.get(`${BASE}/pond-cycles/active/:pondId`, () => err(404)),
      http.post(`${BASE}/pond-cycles`, () => {
        created = true;
        return ok(fx.activeCycle);
      }),
    );
    renderWithProviders(<AppRoutes />, { route: ROUTE, authed: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /start cycle/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/shrimp count/i), '400000');
    await user.click(within(dialog).getByRole('button', { name: /start cycle/i }));
    await waitFor(() => expect(created).toBe(true));
  });
});
