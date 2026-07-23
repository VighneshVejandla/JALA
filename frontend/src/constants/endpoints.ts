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
  roles: {
    base: '/roles',
  },
  users: {
    base: '/users',
    byId: (id: string) => `/users/${id}`,
    activate: (id: string) => `/users/${id}/activate`,
    deactivate: (id: string) => `/users/${id}/deactivate`,
    sites: (id: string) => `/users/${id}/sites`,
    site: (id: string, siteId: string) => `/users/${id}/sites/${siteId}`,
  },
  sites: {
    base: '/sites',
    byId: (id: string) => `/sites/${id}`,
    activate: (id: string) => `/sites/${id}/activate`,
    deactivate: (id: string) => `/sites/${id}/deactivate`,
  },
  ponds: {
    base: '/ponds',
    bySite: (siteId: string) => `/ponds/site/${siteId}`,
    byId: (id: string) => `/ponds/${id}`,
    activate: (id: string) => `/ponds/${id}/activate`,
    deactivate: (id: string) => `/ponds/${id}/deactivate`,
  },
  pondCycles: {
    base: '/pond-cycles',
    active: (pondId: string) => `/pond-cycles/active/${pondId}`,
    byPond: (pondId: string) => `/pond-cycles/pond/${pondId}`,
    byId: (id: string) => `/pond-cycles/${id}`,
    harvest: (id: string) => `/pond-cycles/${id}/harvest`,
    undoHarvest: (id: string) => `/pond-cycles/${id}/undo-harvest`,
  },
  feedSchedules: {
    base: '/feed-schedules',
    byCycle: (cycleId: string) => `/feed-schedules/cycle/${cycleId}`,
    byId: (id: string) => `/feed-schedules/${id}`,
    activate: (id: string) => `/feed-schedules/${id}/activate`,
    deactivate: (id: string) => `/feed-schedules/${id}/deactivate`,
  },
  feedEntries: {
    base: '/feed-entries',
    byId: (id: string) => `/feed-entries/${id}`,
    cancel: (id: string) => `/feed-entries/${id}/cancel`,
    restore: (id: string) => `/feed-entries/${id}/restore`,
  },
  medicines: {
    base: '/medicines',
    byId: (id: string) => `/medicines/${id}`,
    cancel: (id: string) => `/medicines/${id}/cancel`,
    restore: (id: string) => `/medicines/${id}/restore`,
  },
  medicinePhotos: {
    base: '/medicine-photos',
  },
  harvests: {
    base: '/harvests',
    cancel: (id: string) => `/harvests/${id}/cancel`,
  },
  history: {
    cycles: (pondId: string) => `/history/pond/${pondId}/cycles`,
    harvests: (pondId: string) => `/history/pond/${pondId}/harvests`,
    feeds: (pondId: string) => `/history/pond/${pondId}/feeds`,
    medicines: (pondId: string) => `/history/pond/${pondId}/medicines`,
    timeline: (pondId: string) => `/history/pond/${pondId}/timeline`,
  },
  exports: {
    revenueExcel: '/export/revenue/excel',
    revenuePdf: '/export/revenue/pdf',
    feedExcel: '/export/feed/excel',
    feedPdf: '/export/feed/pdf',
    medicineExcel: '/export/medicine/excel',
    medicinePdf: '/export/medicine/pdf',
  },
  analytics: {
    feedPond: (pondId: string) => `/analytics/feed/pond/${pondId}`,
    feedSite: (siteId: string) => `/analytics/feed/site/${siteId}`,
    harvestPond: (pondId: string) => `/analytics/harvest/pond/${pondId}`,
    harvestSite: (siteId: string) => `/analytics/harvest/site/${siteId}`,
    inventorySite: (siteId: string) => `/analytics/inventory/site/${siteId}`,
  },
  reports: {
    revenueChart: (siteId: string) => `/reports/chart/revenue/${siteId}`,
    feedChart: (siteId: string) => `/reports/chart/feed/${siteId}`,
    harvestChart: (siteId: string) => `/reports/chart/harvest/${siteId}`,
  },
  feedInventory: {
    base: '/feed-inventory',
    bySite: (siteId: string) => `/feed-inventory/${siteId}`,
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
    sites: (deliveryId: string) => `/feed-deliveries/${deliveryId}/sites`,
  },
  siteDeliveryReceipts: {
    base: '/site-delivery-receipts',
  },
  search: {
    base: '/search',
  },
} as const;
