import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/server-ref';
import * as fx from '@/test/fixtures';
import { renderWithProviders } from '@/test/utils';
import { AppRoutes } from '@/app/router';
import { ErrorBoundary } from '@/app/ErrorBoundary';
import { ServerError } from '@/features/misc/ServerError';

const BASE = '*/api/v1';
function ok<T>(data: T) {
  return HttpResponse.json({ success: true, message: 'ok', data, timestamp: '' });
}

describe('Forbidden (403)', () => {
  it('renders the access-denied page', async () => {
    server.use(http.get(`${BASE}/auth/me`, () => ok(fx.adminUser)));
    renderWithProviders(<AppRoutes />, { route: '/403', authed: true });
    expect(await screen.findByText(/access denied/i)).toBeInTheDocument();
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: /go home/i }));
    expect(await screen.findByText('Total Sites')).toBeInTheDocument();
  });

  it('sends an unauthenticated user to login', async () => {
    renderWithProviders(<AppRoutes />, { route: '/403' });
    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: /go home/i }));
    expect(await screen.findByText('JALA Aqua Management')).toBeInTheDocument();
  });
});

describe('ServerError (500) + ErrorBoundary', () => {
  it('renders the 500 screen', () => {
    const onReset = vi.fn();
    render(<ServerError onReset={onReset} />);
    expect(screen.getByText(/something went wrong/i)).toBeInTheDocument();
  });

  it('catches a render crash and shows the 500 screen', () => {
    const Boom = () => {
      throw new Error('boom');
    };
    // Silence the expected React error log for this test.
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
    render(
      <MemoryRouter>
        <ErrorBoundary>
          <Boom />
        </ErrorBoundary>
      </MemoryRouter>,
    );
    expect(screen.getByText(/something went wrong/i)).toBeInTheDocument();
    spy.mockRestore();
  });
});
