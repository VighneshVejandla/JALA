/**
 * Client-side route paths. The three experience roots double as post-login
 * landing pages (see auth/roles.homePathFor).
 */
export const ROUTES = {
  login: '/login',
  root: '/',

  // User experience
  app: '/app',
  ponds: '/app/ponds',
  pondDetail: (pondId: string) => `/app/ponds/${pondId}`,
  appAlerts: '/app/alerts',
  appProfile: '/app/profile',

  // Driver experience
  driver: '/driver',
  driverProfile: '/driver/profile',

  // Admin experience
  admin: '/admin',
  adminSites: '/admin/sites',
  adminUsers: '/admin/users',
  adminAlerts: '/admin/alerts',
  adminProfile: '/admin/profile',
} as const;
