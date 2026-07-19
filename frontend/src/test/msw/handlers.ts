import { http, HttpResponse } from 'msw';
import * as fx from '../fixtures';

const BASE = '*/api/v1';

/** Wrap a payload in the backend's ApiResponse envelope. */
function ok<T>(data: T, message = 'ok') {
  return HttpResponse.json({
    success: true,
    message,
    data,
    timestamp: '2026-07-18T00:00:00',
  });
}

/** Default happy-path handlers. Individual tests override as needed. */
export const handlers = [
  http.post(`${BASE}/auth/login`, async ({ request }) => {
    const body = (await request.json()) as { employeeCode: string };
    if (body.employeeCode === 'BAD') {
      return HttpResponse.json(
        { success: false, message: 'Bad credentials', data: null, timestamp: '' },
        { status: 401 },
      );
    }
    return ok(fx.loginResponse, 'Login successful');
  }),

  http.get(`${BASE}/auth/me`, () => ok(fx.workerUser)),

  http.get(`${BASE}/sites`, () => ok(fx.sites)),
  http.get(`${BASE}/ponds/site/:siteId`, () => ok(fx.ponds)),
  http.get(`${BASE}/dashboard/home/:siteId`, () => ok(fx.homeDashboard)),
  http.get(`${BASE}/dashboard/:pondId`, () => ok(fx.pondDashboard)),

  http.get(`${BASE}/notifications`, () => ok(fx.notifications)),
  http.patch(`${BASE}/notifications/:id/read`, () =>
    ok({ ...fx.notifications[0], status: 'READ' }),
  ),

  http.get(`${BASE}/feed-deliveries`, () => ok(fx.deliveries)),
  http.post(`${BASE}/feed-deliveries`, () =>
    ok({ ...fx.deliveries[0], id: 'd-new' }),
  ),

  http.get(`${BASE}/users`, () => ok([fx.adminUser, fx.workerUser, fx.driverUser])),
];
