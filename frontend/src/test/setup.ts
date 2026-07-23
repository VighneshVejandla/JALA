import '@testing-library/jest-dom/vitest';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';
import { cleanup } from '@testing-library/react';
import { server } from './msw/server';

// --- localStorage polyfill (jsdom's is not always fully wired under vitest) ---
if (typeof localStorage === 'undefined' || typeof localStorage.clear !== 'function') {
  const store = new Map<string, string>();
  const mock: Storage = {
    getItem: (k) => (store.has(k) ? (store.get(k) as string) : null),
    setItem: (k, v) => void store.set(k, String(v)),
    removeItem: (k) => void store.delete(k),
    clear: () => store.clear(),
    key: (i) => Array.from(store.keys())[i] ?? null,
    get length() {
      return store.size;
    },
  };
  Object.defineProperty(window, 'localStorage', { value: mock, configurable: true });
}

// --- jsdom shims required by Radix UI primitives ---
if (!window.matchMedia) {
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  }));
}

class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}
window.ResizeObserver = window.ResizeObserver ?? (ResizeObserverStub as never);

// Radix Select/Dialog use these; jsdom does not implement them.
Element.prototype.scrollIntoView = Element.prototype.scrollIntoView ?? vi.fn();
Element.prototype.hasPointerCapture =
  Element.prototype.hasPointerCapture ?? vi.fn(() => false);
Element.prototype.setPointerCapture =
  Element.prototype.setPointerCapture ?? vi.fn();
Element.prototype.releasePointerCapture =
  Element.prototype.releasePointerCapture ?? vi.fn();

// Blob URL helpers used by the export "save as" path.
if (typeof URL.createObjectURL !== 'function') {
  URL.createObjectURL = vi.fn(() => 'blob:mock');
  URL.revokeObjectURL = vi.fn();
}

// --- MSW lifecycle ---
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  server.resetHandlers();
  cleanup();
  localStorage.clear();
});
afterAll(() => server.close());
