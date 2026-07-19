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
const asDriver = () =>
  server.use(http.get(`${BASE}/auth/me`, () => ok(fx.driverUser)));

describe('DriverDeliveryDetail', () => {
  it('navigates from the list into a delivery detail', async () => {
    asDriver();
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    const user = userEvent.setup();
    await user.click(await screen.findByText('Morning run'));
    expect(await screen.findByText('Drop-offs')).toBeInTheDocument();
    expect(await screen.findByText('North Farm')).toBeInTheDocument();
  });

  it('ignores non-Enter keys on a delivery card', async () => {
    asDriver();
    renderWithProviders(<AppRoutes />, { route: '/driver', authed: true });
    const user = userEvent.setup();
    const card = (await screen.findByText('Morning run')).closest(
      '[role="button"]',
    ) as HTMLElement;
    card.focus();
    await user.keyboard('a');
    expect(screen.getByText('Morning run')).toBeInTheDocument();
  });

  it('adds a site drop-off', async () => {
    asDriver();
    let added = false;
    server.use(
      http.post(`${BASE}/feed-deliveries/:deliveryId/sites`, () => {
        added = true;
        return ok(fx.siteDeliveries[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/driver/deliveries/d-1',
      authed: true,
    });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: /add drop-off/i }));
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('combobox'));
    await user.click(await screen.findByRole('option', { name: /North Farm/i }));
    await user.type(within(dialog).getByLabelText(/number of bags/i), '10');
    await user.click(within(dialog).getByRole('button', { name: /add drop-off/i }));
    await waitFor(() => expect(added).toBe(true));
  });

  it('uploads a receipt for a drop-off', async () => {
    asDriver();
    let uploaded = false;
    server.use(
      http.post(`${BASE}/site-delivery-receipts`, () => {
        uploaded = true;
        return ok(fx.receipts[0]);
      }),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/driver/deliveries/d-1',
      authed: true,
    });
    const user = userEvent.setup();
    const input = await screen.findByLabelText(/receipt for North Farm/i);
    await user.upload(input, new File(['x'], 'r.png', { type: 'image/png' }));
    await waitFor(() => expect(uploaded).toBe(true));
  });

  it('shows an error when the delivery fails to load', async () => {
    asDriver();
    server.use(
      http.get(`${BASE}/feed-deliveries/:id`, () =>
        HttpResponse.json(
          { success: false, message: 'x', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/driver/deliveries/d-1',
      authed: true,
    });
    expect(
      await screen.findByText(/could not load this delivery/i),
    ).toBeInTheDocument();
  });

  it('renders a drop-off with null bag/weight fields', async () => {
    asDriver();
    server.use(
      http.get(`${BASE}/feed-deliveries/:deliveryId/sites`, () =>
        ok([{ ...fx.siteDeliveries[0], numberOfBags: null, totalKg: null }]),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/driver/deliveries/d-1',
      authed: true,
    });
    expect(await screen.findByText('North Farm')).toBeInTheDocument();
  });

  it('surfaces an add-drop error', async () => {
    asDriver();
    server.use(
      http.post(`${BASE}/feed-deliveries/:deliveryId/sites`, () =>
        HttpResponse.json(
          { success: false, message: 'Drop failed', data: null, timestamp: '' },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/driver/deliveries/d-1',
      authed: true,
    });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /add drop-off/i }));
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('combobox'));
    await user.click(await screen.findByRole('option', { name: /North Farm/i }));
    await user.type(within(dialog).getByLabelText(/number of bags/i), '3');
    await user.click(within(dialog).getByRole('button', { name: /add drop-off/i }));
    expect(await screen.findByText('Drop failed')).toBeInTheDocument();
  });

  it('shows an empty drop-off state', async () => {
    asDriver();
    server.use(
      http.get(`${BASE}/feed-deliveries/:deliveryId/sites`, () => ok([])),
    );
    renderWithProviders(<AppRoutes />, {
      route: '/driver/deliveries/d-1',
      authed: true,
    });
    expect(await screen.findByText(/no drop-offs yet/i)).toBeInTheDocument();
  });
});
