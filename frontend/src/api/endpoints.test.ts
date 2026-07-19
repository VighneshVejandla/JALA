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
    expect(await api.notifications.list()).toHaveLength(2);
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
  });
});
