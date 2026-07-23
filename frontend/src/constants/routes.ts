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
  driverDeliveryDetail: (id: string) => `/driver/deliveries/${id}`,
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
  adminInventory: '/admin/inventory',
  adminAnalytics: '/admin/analytics',
  adminReports: '/admin/reports',
  adminSearch: '/admin/search',
  adminDeliveries: '/admin/deliveries',
  adminDeliveryDetail: (id: string) => `/admin/deliveries/${id}`,
  adminAlerts: '/admin/alerts',
  adminProfile: '/admin/profile',
} as const;
