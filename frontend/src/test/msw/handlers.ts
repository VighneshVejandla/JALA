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

  http.get(`${BASE}/notifications`, () =>
    ok({
      unreadCount: fx.notifications.filter((n) => n.status === 'UNREAD').length,
      notifications: fx.notifications,
    }),
  ),
  http.patch(`${BASE}/notifications/:id/read`, () =>
    ok({ ...fx.notifications[0], status: 'READ' }),
  ),

  http.get(`${BASE}/feed-deliveries`, () => ok(fx.deliveries)),
  http.post(`${BASE}/feed-deliveries`, () =>
    ok({ ...fx.deliveries[0], id: 'd-new' }),
  ),

  http.get(`${BASE}/users`, () => ok([fx.adminUser, fx.workerUser, fx.driverUser])),
  http.post(`${BASE}/users`, () => ok(fx.adminUser)),
  http.patch(`${BASE}/users/:id/activate`, () => ok(null)),
  http.patch(`${BASE}/users/:id`, () => ok(fx.adminUser)),
  http.patch(`${BASE}/users/:id/deactivate`, () => ok(null)),

  http.get(`${BASE}/roles`, () => ok(fx.roles)),

  http.get(`${BASE}/ponds`, () => ok(fx.ponds)),
  http.get(`${BASE}/feed-inventory/:siteId`, () =>
    ok({
      id: 'inv-1',
      siteId: 'site-1',
      siteCode: 'S-001',
      siteName: 'North Farm',
      totalReceivedKg: 5000,
      totalConsumedKg: 3000,
      availableKg: 2000,
    }),
  ),
  http.get(`${BASE}/users/:id/sites`, () => ok(['site-1'])),
  http.post(`${BASE}/users/:id/sites/:siteId`, () => ok(null)),
  http.delete(`${BASE}/users/:id/sites/:siteId`, () => ok(null)),
  http.get(`${BASE}/notifications/unread-count`, () => ok(2)),
  http.patch(`${BASE}/pond-cycles/:id/undo-harvest`, () => ok(null)),

  http.get(`${BASE}/sites/:id`, () => ok(fx.sites[0])),
  http.post(`${BASE}/sites`, () => ok(fx.sites[0])),
  http.get(`${BASE}/ponds/:id`, () => ok(fx.ponds[0])),
  http.post(`${BASE}/ponds`, () => ok(fx.ponds[0])),

  http.get(`${BASE}/pond-cycles/active/:pondId`, () => ok(fx.activeCycle)),
  http.get(`${BASE}/pond-cycles/pond/:pondId`, () => ok([fx.activeCycle])),
  http.post(`${BASE}/pond-cycles`, () => ok(fx.activeCycle)),
  http.patch(`${BASE}/pond-cycles/:id/harvest`, () => ok(null)),

  http.get(`${BASE}/feed-schedules/cycle/:cycleId`, () => ok(fx.feedSchedules)),
  http.post(`${BASE}/feed-schedules`, () => ok(fx.feedSchedules)),

  http.get(`${BASE}/feed-entries`, () => ok(fx.feedEntries)),
  http.post(`${BASE}/feed-entries`, () => ok(fx.feedEntries[0])),

  http.get(`${BASE}/medicines`, () => ok(fx.medicines)),
  http.post(`${BASE}/medicines`, () => ok(fx.medicines[0])),
  http.post(`${BASE}/medicine-photos`, () => ok({ id: 'mp-1' })),

  http.get(`${BASE}/harvests`, () => ok(fx.harvests)),
  http.post(`${BASE}/harvests`, () => ok(fx.harvests[0])),

  http.get(`${BASE}/analytics/feed/pond/:pondId`, () => ok(fx.feedAnalytics)),
  http.get(`${BASE}/analytics/harvest/pond/:pondId`, () =>
    ok(fx.harvestAnalytics),
  ),
  http.get(`${BASE}/analytics/inventory/site/:siteId`, () =>
    ok(fx.inventoryAnalytics),
  ),
  http.get(`${BASE}/analytics/feed/site/:siteId/daily`, () => ok(fx.dailyFeed)),
  http.get(`${BASE}/analytics/feed/site/:siteId`, () => ok(fx.siteFeedAnalytics)),
  http.get(`${BASE}/analytics/harvest/site/:siteId`, () =>
    ok(fx.siteHarvestAnalytics),
  ),
  http.get(`${BASE}/reports/chart/revenue/:siteId`, () => ok(fx.monthlyChart)),
  http.get(`${BASE}/reports/chart/feed/:siteId`, () => ok(fx.monthlyChart)),
  http.get(`${BASE}/reports/chart/harvest/:siteId`, () => ok(fx.monthlyChart)),
  http.get(`${BASE}/feed-inventory`, () => ok(fx.feedInventoryList)),
  http.get(`${BASE}/search`, () => ok(fx.globalSearch)),
  http.get(`${BASE}/medicine-photos`, () => ok(fx.medicinePhotos)),
  http.get(`${BASE}/users/:id/sites`, () => ok(['site-1'])),
  http.patch(`${BASE}/users/:id`, () => ok(fx.workerUser)),
  http.post(`${BASE}/users/:id/sites/:siteId`, () => ok(null)),
  http.delete(`${BASE}/users/:id/sites/:siteId`, () => ok(null)),
  http.patch(`${BASE}/feed-entries/:id`, () => ok(fx.feedEntries[0])),
  http.patch(`${BASE}/feed-entries/:id/cancel`, () => ok(fx.feedEntries[0])),
  http.patch(`${BASE}/feed-entries/:id/restore`, () => ok(fx.feedEntries[0])),
  http.patch(`${BASE}/medicines/:id/cancel`, () => ok(fx.medicines[0])),
  http.patch(`${BASE}/medicines/:id/restore`, () => ok(fx.medicines[0])),

  http.get(`${BASE}/history/pond/:pondId/cycles`, () =>
    ok([
      {
        cycleId: 'cycle-1',
        cycleNumber: 3,
        currentCycle: true,
        status: 'ACTIVE',
        species: 'VANNAMEI',
        stockingDate: '2026-05-01',
        shrimpCount: 500000,
        harvestDate: null,
        totalFeedEntries: 12,
        totalMedicineEntries: 2,
        totalHarvests: 1,
      },
    ]),
  ),
  http.get(`${BASE}/history/pond/:pondId/feeds`, () =>
    ok([
      {
        feedEntryId: 'fe-1',
        cycleNumber: 3,
        sessionNumber: 1,
        feedDate: '2026-07-18',
        feedSize: 'ONE',
        feedQuantityKg: 25,
        remarks: null,
        status: 'ACTIVE',
        createdBy: 'EMP-WORK',
      },
    ]),
  ),
  http.get(`${BASE}/history/pond/:pondId/medicines`, () =>
    ok([
      {
        medicineId: 'med-1',
        cycleNumber: 3,
        quantity: 3,
        unit: 'ML',
        remarks: null,
        status: 'ACTIVE',
        createdBy: 'EMP-WORK',
        createdAt: '2026-07-18T09:00:00',
        photos: [{ photoId: 'p1', fileName: 'a.jpg', filePath: '/a.jpg' }],
      },
    ]),
  ),
  http.get(`${BASE}/history/pond/:pondId/harvests`, () => ok(fx.harvestHistory)),
  http.get(`${BASE}/history/pond/:pondId/timeline`, () =>
    ok({ pondId: 'pond-1', pondCode: 'P-01', pondName: 'Pond One', timeline: [] }),
  ),

  http.get(`${BASE}/feed-deliveries/:deliveryId/sites`, () =>
    ok(fx.siteDeliveries),
  ),
  http.post(`${BASE}/feed-deliveries/:deliveryId/sites`, () =>
    ok(fx.siteDeliveries[0]),
  ),
  http.get(`${BASE}/feed-deliveries/:id`, () => ok(fx.deliveries[0])),
  http.get(`${BASE}/site-delivery-receipts`, () => ok(fx.receipts)),
  http.post(`${BASE}/site-delivery-receipts`, () => ok(fx.receipts[0])),

  http.patch(`${BASE}/sites/:id/activate`, () => ok(null)),
  http.patch(`${BASE}/sites/:id/deactivate`, () => ok(null)),
  http.patch(`${BASE}/ponds/:id/activate`, () => ok(null)),
  http.patch(`${BASE}/ponds/:id/deactivate`, () => ok(null)),

  http.post(`${BASE}/export/:kind/:format`, () =>
    HttpResponse.arrayBuffer(new ArrayBuffer(8), {
      headers: { 'Content-Type': 'application/octet-stream' },
    }),
  ),
];
