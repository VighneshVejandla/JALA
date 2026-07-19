import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/server-ref';
import * as fx from '@/test/fixtures';
import { api } from './endpoints';

const BASE = '*/api/v1';
function ok<T>(data: T) {
  return HttpResponse.json({ success: true, message: 'ok', data, timestamp: '' });
}

describe('api endpoint wrappers', () => {
  it('auth', async () => {
    expect((await api.auth.login({ employeeCode: 'x', password: 'y' })).accessToken).toBe(
      'test-token',
    );
    expect((await api.auth.me()).role).toBe('WORKER');
  });

  it('sites', async () => {
    server.use(http.get(`${BASE}/sites/:id`, () => ok(fx.sites[0])));
    expect(await api.sites.list()).toHaveLength(2);
    expect((await api.sites.byId('site-1')).siteCode).toBe('S-001');
  });

  it('ponds', async () => {
    server.use(
      http.get(`${BASE}/ponds`, () => ok(fx.ponds)),
      http.get(`${BASE}/ponds/:id`, () => ok(fx.ponds[0])),
    );
    expect(await api.ponds.list()).toHaveLength(1);
    expect(await api.ponds.bySite('site-1')).toHaveLength(1);
    expect((await api.ponds.byId('pond-1')).pondCode).toBe('P-01');
  });

  it('dashboard', async () => {
    expect((await api.dashboard.home('site-1')).siteName).toBe('North Farm');
    expect((await api.dashboard.pond('pond-1')).pondName).toBe('Pond One');
  });

  it('notifications', async () => {
    server.use(http.get(`${BASE}/notifications/unread-count`, () => ok(3)));
    expect((await api.notifications.list()).notifications).toHaveLength(2);
    expect(await api.notifications.unreadCount()).toBe(3);
    expect((await api.notifications.markRead('n-1')).status).toBe('READ');
  });

  it('feed deliveries', async () => {
    server.use(http.get(`${BASE}/feed-deliveries/:id`, () => ok(fx.deliveries[0])));
    expect(await api.feedDeliveries.list()).toHaveLength(1);
    expect((await api.feedDeliveries.byId('d-1')).status).toBe('IN_PROGRESS');
    expect((await api.feedDeliveries.create({ remarks: 'go' })).id).toBe('d-new');
  });

  it('users', async () => {
    expect(await api.users.list()).toHaveLength(3);
    await api.users.create({
      roleId: 'r',
      employeeCode: 'E',
      fullName: 'N',
      password: 'longpassword1',
    });
    await api.users.update('u-admin', { fullName: 'X' });
    await api.users.activate('u-admin');
    await api.users.deactivate('u-admin');
    expect(await api.users.sites('u-admin')).toEqual(['site-1']);
    await api.users.addSite('u-admin', 'site-1');
    await api.users.removeSite('u-admin', 'site-1');
  });

  it('roles', async () => {
    expect(await api.roles.list()).toHaveLength(3);
  });

  it('pond cycles, schedules, feed entries', async () => {
    expect((await api.pondCycles.active('pond-1')).status).toBe('ACTIVE');
    expect(await api.pondCycles.byPond('pond-1')).toHaveLength(1);
    await api.pondCycles.create({
      pondId: 'pond-1',
      species: 'VANNAMEI',
      stockingDate: '2026-05-01',
      shrimpCount: 100,
    });
    await api.pondCycles.harvest('cycle-1');
    await api.pondCycles.undoHarvest('cycle-1');
    expect(await api.feedSchedules.byCycle('cycle-1')).toHaveLength(1);
    await api.feedSchedules.create({ pondCycleId: 'cycle-1', feedingTimes: ['08:00'] });
    expect(await api.feedEntries.list('cycle-1', '2026-07-18')).toHaveLength(1);
    await api.feedEntries.create({
      pondCycleId: 'cycle-1',
      feedScheduleId: 'sch-1',
      feedDate: '2026-07-18',
      feedSize: 'ONE',
      feedQuantityKg: 10,
    });
  });

  it('medicines, photos, harvests', async () => {
    expect(await api.medicines.list('cycle-1')).toHaveLength(1);
    await api.medicines.create({ pondCycleId: 'cycle-1', quantity: 1, unit: 'ML' });
    const form = new FormData();
    form.append('medicineEntryId', 'med-1');
    form.append('file', new File(['x'], 'a.png', { type: 'image/png' }));
    await api.medicinePhotos.upload(form);
    expect(await api.harvests.list('cycle-1')).toHaveLength(1);
    const hf = new FormData();
    hf.append('pondCycleId', 'cycle-1');
    await api.harvests.create(hf);
  });

  it('history', async () => {
    expect(await api.history.cycles('pond-1')).toHaveLength(1);
    expect(await api.history.feeds('pond-1')).toHaveLength(1);
    expect(await api.history.medicines('pond-1')).toHaveLength(1);
    expect(await api.history.harvests('pond-1')).toHaveLength(1);
    expect((await api.history.timeline('pond-1')).pondCode).toBe('P-01');
  });

  it('analytics and inventory', async () => {
    expect((await api.analytics.feedPond('pond-1')).monthFeedKg).toBe(720);
    expect((await api.analytics.harvestPond('pond-1')).harvestCount).toBe(1);
    expect((await api.analytics.inventorySite('site-1')).availableBags).toBe(80);
    expect((await api.feedInventory.bySite('site-1')).availableKg).toBe(2000);
  });

  it('exports return blobs', async () => {
    const filter = { siteId: 'site-1', fromDate: '2026-06-01', toDate: '2026-07-01' };
    expect(await api.exports.revenueExcel(filter)).toBeInstanceOf(Blob);
    expect(await api.exports.revenuePdf(filter)).toBeInstanceOf(Blob);
    expect(await api.exports.feedExcel(filter)).toBeInstanceOf(Blob);
    expect(await api.exports.feedPdf(filter)).toBeInstanceOf(Blob);
    expect(await api.exports.medicineExcel(filter)).toBeInstanceOf(Blob);
    expect(await api.exports.medicinePdf(filter)).toBeInstanceOf(Blob);
  });
});
