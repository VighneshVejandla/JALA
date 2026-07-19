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
  driverAlerts: '/driver/alerts',
  driverProfile: '/driver/profile',

  // Admin experience
  admin: '/admin',
  adminSites: '/admin/sites',
  adminSiteDetail: (siteId: string) => `/admin/sites/${siteId}`,
  adminPondManage: (pondId: string) => `/admin/ponds/${pondId}`,
  adminUsers: '/admin/users',
  adminHarvested: '/admin/harvested',
  adminHistory: '/admin/history',
  adminAlerts: '/admin/alerts',
  adminProfile: '/admin/profile',
} as const;
