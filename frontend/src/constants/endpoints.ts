/**
 * Single source of truth for backend REST paths (relative to the API base
 * URL, which already includes `/api/v1`). Never hard-code a path elsewhere —
 * import from here so a backend route change is a one-line edit.
 */
export const ENDPOINTS = {
  auth: {
    login: '/auth/login',
    me: '/auth/me',
  },
  sites: {
    base: '/sites',
    byId: (id: string) => `/sites/${id}`,
  },
  ponds: {
    base: '/ponds',
    bySite: (siteId: string) => `/ponds/site/${siteId}`,
    byId: (id: string) => `/ponds/${id}`,
  },
  dashboard: {
    home: (siteId: string) => `/dashboard/home/${siteId}`,
    pond: (pondId: string) => `/dashboard/${pondId}`,
  },
  notifications: {
    base: '/notifications',
    unreadCount: '/notifications/unread-count',
    read: (id: string) => `/notifications/${id}/read`,
  },
  feedDeliveries: {
    base: '/feed-deliveries',
    byId: (id: string) => `/feed-deliveries/${id}`,
  },
  users: {
    base: '/users',
  },
} as const;
