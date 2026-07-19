import type {
  FeedDeliveryResponse,
  HomeDashboardResponse,
  LoginResponse,
  NotificationResponse,
  PondDashboardResponse,
  PondResponse,
  SiteResponse,
  UserResponse,
} from '@/api/types';

export const adminUser: UserResponse = {
  id: 'u-admin',
  employeeCode: 'EMP-ADMIN',
  fullName: 'Ada Admin',
  email: 'ada@jala.test',
  phone: '9990001111',
  role: 'ADMIN',
  isActive: true,
};

export const workerUser: UserResponse = {
  id: 'u-worker',
  employeeCode: 'EMP-WORK',
  fullName: 'Will Worker',
  email: null,
  phone: null,
  role: 'WORKER',
  isActive: true,
};

export const driverUser: UserResponse = {
  id: 'u-driver',
  employeeCode: 'EMP-DRIVE',
  fullName: 'Dan Driver',
  email: 'dan@jala.test',
  phone: null,
  role: 'DRIVER',
  isActive: true,
};

export const loginResponse: LoginResponse = {
  accessToken: 'test-token',
  tokenType: 'Bearer',
  expiresIn: 3600,
  employeeCode: 'EMP-WORK',
  fullName: 'Will Worker',
  role: 'WORKER',
};

export const sites: SiteResponse[] = [
  {
    id: 'site-1',
    siteCode: 'S-001',
    siteName: 'North Farm',
    ownerName: 'Owner One',
    location: 'Coast Rd',
    totalAcres: 12,
    isActive: true,
  },
  {
    id: 'site-2',
    siteCode: 'S-002',
    siteName: 'South Farm',
    ownerName: null,
    location: null,
    totalAcres: null,
    isActive: false,
  },
];

export const ponds: PondResponse[] = [
  {
    id: 'pond-1',
    siteId: 'site-1',
    siteName: 'North Farm',
    pondCode: 'P-01',
    pondName: 'Pond One',
    pondAcres: 2.5,
    isActive: true,
  },
];

export const homeDashboard: HomeDashboardResponse = {
  siteId: 'site-1',
  siteCode: 'S-001',
  siteName: 'North Farm',
  totalPonds: 4,
  activeCycles: 2,
  todayFeedKg: 120,
  availableFeedKg: 40,
  todayHarvestKg: 300,
  todayRevenue: 45000,
  unreadNotifications: 3,
  lowInventory: true,
};

export const pondDashboard: PondDashboardResponse = {
  pondId: 'pond-1',
  pondCode: 'P-01',
  pondName: 'Pond One',
  siteId: 'site-1',
  siteCode: 'S-001',
  siteName: 'North Farm',
  activeCycleId: 'cycle-1',
  cycleNumber: 3,
  cycleStatus: 'ACTIVE',
  stockingCompleted: true,
  species: 'VANNAMEI',
  stockingDate: '2026-05-01',
  daysSinceStocking: 60,
  shrimpCount: 500000,
  todayFeedKg: 30,
  totalFeedKg: 900,
  todayFeedEntries: 2,
  medicineEntryCount: 5,
  totalMedicineQuantity: 12,
  lastMedicineDate: '2026-06-20',
  medicinePhotoCount: 3,
  harvestCount: 1,
  lastHarvestDate: '2026-06-30',
  lastHarvestQuantityKg: 250,
  lastHarvestAmount: 38000,
  lastBuyerName: 'Buyer Co',
};

export const notifications: NotificationResponse[] = [
  {
    id: 'n-1',
    type: 'LOW_INVENTORY',
    title: 'Low feed',
    message: 'Feed running low at North Farm',
    siteId: 'site-1',
    pondId: null,
    status: 'UNREAD',
    createdAt: '2026-07-18T09:00:00',
    readAt: null,
  },
  {
    id: 'n-2',
    type: 'INFO',
    title: 'Cycle started',
    message: 'New cycle on Pond One',
    siteId: 'site-1',
    pondId: 'pond-1',
    status: 'READ',
    createdAt: '2026-07-17T09:00:00',
    readAt: '2026-07-17T10:00:00',
  },
];

export const deliveries: FeedDeliveryResponse[] = [
  {
    id: 'd-1',
    deliveredBy: 'Dan Driver',
    deliveredAt: '2026-07-18T08:00:00',
    remarks: 'Morning run',
    status: 'IN_PROGRESS',
  },
];

/** A site whose inventory is healthy (exercises the non-warning branches). */
export const homeDashboardHealthy: HomeDashboardResponse = {
  ...homeDashboard,
  lowInventory: false,
  unreadNotifications: 0,
};

/** A pond with no harvests yet (hides the "last harvest" card). */
export const pondDashboardNoHarvest: PondDashboardResponse = {
  ...pondDashboard,
  harvestCount: 0,
  lastHarvestDate: null,
  lastHarvestQuantityKg: null,
  lastHarvestAmount: null,
  lastBuyerName: null,
};

/** Deliveries covering every status tone. */
export const deliveriesAllStatuses: FeedDeliveryResponse[] = [
  { ...deliveries[0], id: 'd-a', status: 'COMPLETED', remarks: null },
  { ...deliveries[0], id: 'd-b', status: 'CANCELLED', deliveredAt: null },
  { ...deliveries[0], id: 'd-c', deliveredBy: null, status: 'PENDING' },
];

/** An inactive user with no email (exercises those branches on the users list). */
export const inactiveUser: UserResponse = {
  id: 'u-inactive',
  employeeCode: 'EMP-OFF',
  fullName: 'Ines Inactive',
  email: null,
  phone: null,
  role: 'SUPERVISOR',
  isActive: false,
};
