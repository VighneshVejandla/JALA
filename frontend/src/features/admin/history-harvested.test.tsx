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

async function pickPond(user: ReturnType<typeof userEvent.setup>) {
  // Two comboboxes: [0] site selector (pre-filled), [1] pond selector.
  const combos = await screen.findAllByRole('combobox');
  await user.click(combos[combos.length - 1]);
  await user.click(await screen.findByRole('option', { name: /Pond One/i }));
}

describe('HistoryPage', () => {
  it('shows history tabs for a selected pond', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/history', authed: true });
    const user = userEvent.setup();
    await pickPond(user);

    expect(await screen.findByRole('tab', { name: /cycles/i })).toBeInTheDocument();
    // default tab shows a cycle row
    expect(await screen.findByText(/Cycle #3/)).toBeInTheDocument();

    // switch to feeds tab
    await user.click(screen.getByRole('tab', { name: /feeds/i }));
    expect(await screen.findByText(/session 1/i)).toBeInTheDocument();
  });

  it('downloads a report', async () => {
    asAdmin();
    let exported = false;
    server.use(
      http.post(`${BASE}/export/:kind/:format`, () => {
        exported = true;
        return HttpResponse.arrayBuffer(new ArrayBuffer(8), {
          headers: { 'Content-Type': 'application/octet-stream' },
        });
      }),
    );
    // jsdom lacks URL.createObjectURL — stub it for saveBlob.
    URL.createObjectURL = () => 'blob:x';
    URL.revokeObjectURL = () => {};

    renderWithProviders(<AppRoutes />, { route: '/admin/history', authed: true });
    const user = userEvent.setup();

    const excelButtons = await screen.findAllByRole('button', { name: /excel/i });
    await user.click(excelButtons[0]);
    await waitFor(() => expect(exported).toBe(true));

    const pdfButtons = screen.getAllByRole('button', { name: /pdf/i });
    await user.click(pdfButtons[0]);
    await waitFor(() => expect(exported).toBe(true));
  });
});

describe('HarvestedPage', () => {
  it('shows harvest summary and records for a pond', async () => {
    asAdmin();
    renderWithProviders(<AppRoutes />, { route: '/admin/harvested', authed: true });
    const user = userEvent.setup();
    await pickPond(user);

    expect(await screen.findByText('Revenue')).toBeInTheDocument();
    expect(await screen.findByText(/Buyer Co/)).toBeInTheDocument();
    expect(await screen.findByText(/Cycle #3/)).toBeInTheDocument();
  });
});
