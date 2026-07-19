import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw/server';
import {
  ApiError,
  del,
  get,
  post,
  patch,
  postBlob,
  postForm,
  setUnauthorizedHandler,
  tokenStore,
} from './client';

const BASE = '*/api/v1';

function ok<T>(data: T) {
  return HttpResponse.json({ success: true, message: 'ok', data, timestamp: '' });
}

afterEach(() => {
  tokenStore.clear();
  setUnauthorizedHandler(() => {});
});

describe('tokenStore', () => {
  it('sets, gets and clears the token', () => {
    expect(tokenStore.get()).toBeNull();
    tokenStore.set('abc');
    expect(tokenStore.get()).toBe('abc');
    tokenStore.clear();
    expect(tokenStore.get()).toBeNull();
  });
});

describe('request interceptor', () => {
  it('attaches a Bearer header when a token is present', async () => {
    tokenStore.set('my-token');
    let seen: string | null = null;
    server.use(
      http.get(`${BASE}/ping`, ({ request }) => {
        seen = request.headers.get('authorization');
        return ok({ pong: true });
      }),
    );
    await get('/ping');
    expect(seen).toBe('Bearer my-token');
  });

  it('omits the header when there is no token', async () => {
    let seen: string | null = 'x';
    server.use(
      http.get(`${BASE}/ping`, ({ request }) => {
        seen = request.headers.get('authorization');
        return ok({});
      }),
    );
    await get('/ping');
    expect(seen).toBeNull();
  });
});

describe('verb helpers unwrap ApiResponse.data', () => {
  beforeEach(() => {
    server.use(
      http.get(`${BASE}/thing`, () => ok({ id: 1 })),
      http.post(`${BASE}/thing`, () => ok({ created: true })),
      http.patch(`${BASE}/thing`, () => ok({ patched: true })),
      http.delete(`${BASE}/thing`, () => ok({ deleted: true })),
      http.post(`${BASE}/upload`, () => ok({ uploaded: true })),
      http.post(`${BASE}/blob`, () =>
        HttpResponse.text('binary', {
          headers: { 'Content-Type': 'application/octet-stream' },
        }),
      ),
    );
  });

  it('get', async () => {
    expect(await get<{ id: number }>('/thing')).toEqual({ id: 1 });
  });
  it('post', async () => {
    expect(await post('/thing', { a: 1 })).toEqual({ created: true });
  });
  it('patch', async () => {
    expect(await patch('/thing')).toEqual({ patched: true });
  });
  it('del', async () => {
    expect(await del('/thing')).toEqual({ deleted: true });
  });
  it('postForm', async () => {
    const fd = new FormData();
    fd.append('f', 'x');
    expect(await postForm('/upload', fd)).toEqual({ uploaded: true });
  });
  it('postBlob returns a Blob', async () => {
    const blob = await postBlob('/blob', {});
    expect(blob).toBeInstanceOf(Blob);
  });
});

describe('error handling', () => {
  it('maps a backend error message and status into ApiError', async () => {
    server.use(
      http.get(`${BASE}/boom`, () =>
        HttpResponse.json(
          { success: false, message: 'Nope', data: null, timestamp: '' },
          { status: 400 },
        ),
      ),
    );
    await expect(get('/boom')).rejects.toMatchObject({
      name: 'ApiError',
      status: 400,
      message: 'Nope',
    });
  });

  it('clears the token and invokes the unauthorized handler on 401', async () => {
    tokenStore.set('stale');
    const onUnauth = vi.fn();
    setUnauthorizedHandler(onUnauth);
    server.use(
      http.get(`${BASE}/secure`, () =>
        HttpResponse.json(
          { success: false, message: 'expired', data: null, timestamp: '' },
          { status: 401 },
        ),
      ),
    );
    await expect(get('/secure')).rejects.toBeInstanceOf(ApiError);
    expect(tokenStore.get()).toBeNull();
    expect(onUnauth).toHaveBeenCalledOnce();
  });

  it('reports a friendly message on network failure (status 0)', async () => {
    server.use(http.get(`${BASE}/down`, () => HttpResponse.error()));
    await expect(get('/down')).rejects.toMatchObject({ status: 0 });
  });
});
