import type {
  FeedAnalyticsResponse,
  FeedDeliveryResponse,
  FeedEntryResponse,
  FeedScheduleResponse,
  HarvestHistoryResponse,
  HarvestResponse,
  HomeDashboardResponse,
  InventoryAnalyticsResponse,
  LoginResponse,
  MedicineResponse,
  NotificationResponse,
  PondCycleResponse,
  PondDashboardResponse,
  PondHarvestAnalyticsResponse,
  PondResponse,
  RoleResponse,
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

export const roles: RoleResponse[] = [
  { id: 'role-admin', name: 'ADMIN', description: 'Administrator' },
  { id: 'role-worker', name: 'WORKER', description: 'Field worker' },
  { id: 'role-driver', name: 'DRIVER', description: 'Driver' },
];

export const activeCycle: PondCycleResponse = {
  id: 'cycle-1',
  pondId: 'pond-1',
  pondName: 'Pond One',
  cycleNumber: 3,
  species: 'VANNAMEI',
  stockingDate: '2026-05-01',
  shrimpCount: 500000,
  stockingCompleted: true,
  status: 'ACTIVE',
};

export const feedSchedules: FeedScheduleResponse[] = [
  {
    id: 'sch-1',
    pondCycleId: 'cycle-1',
    sessionNumber: 1,
    feedingTime: '08:00:00',
    isActive: true,
  },
];

export const feedEntries: FeedEntryResponse[] = [
  {
    id: 'fe-1',
    pondCycleId: 'cycle-1',
    feedScheduleId: 'sch-1',
    sessionNumber: 1,
    feedDate: '2026-07-18',
    feedSize: 'ONE',
    feedQuantityKg: 25,
    remarks: null,
    createdBy: 'EMP-WORK',
  },
];

export const medicines: MedicineResponse[] = [
  {
    id: 'med-1',
    pondCycleId: 'cycle-1',
    quantity: 3,
    unit: 'ML',
    remarks: 'Vitamin C',
    status: 'ACTIVE',
    createdBy: 'EMP-WORK',
    createdAt: '2026-07-18T09:00:00',
  },
];

export const harvests: HarvestResponse[] = [
  {
    id: 'h-1',
    pondCycleId: 'cycle-1',
    harvestDate: '2026-06-30',
    harvestQuantityKg: 250,
    quantityDisplay: '250 kg',
    billPhotoPath: '/bills/h-1.jpg',
    buyerName: 'Buyer Co',
    sellingPricePerKg: 152,
    totalAmount: 38000,
    vehicleNumber: 'AP01AB1234',
    remarks: null,
    status: 'ACTIVE',
    uploadedByEmployeeCode: 'EMP-ADMIN',
    uploadedAt: '2026-06-30T12:00:00',
    cancelledByEmployeeCode: null,
    cancelledAt: null,
    cancellationReason: null,
  },
];

export const feedAnalytics: FeedAnalyticsResponse = {
  pondId: 'pond-1',
  pondCode: 'P-01',
  pondName: 'Pond One',
  todayFeedKg: 30,
  weekFeedKg: 180,
  monthFeedKg: 720,
  todayFeedEntries: 2,
  weekFeedEntries: 12,
  monthFeedEntries: 48,
};

export const harvestAnalytics: PondHarvestAnalyticsResponse = {
  pondId: 'pond-1',
  pondCode: 'P-01',
  pondName: 'Pond One',
  harvestCount: 1,
  totalHarvestKg: 250,
  averageHarvestKg: 250,
  totalRevenue: 38000,
  lastHarvestDate: '2026-06-30',
  lastHarvestQuantityKg: 250,
  lastHarvestRevenue: 38000,
  lastBuyer: 'Buyer Co',
};

export const inventoryAnalytics: InventoryAnalyticsResponse = {
  siteId: 'site-1',
  siteCode: 'S-001',
  siteName: 'North Farm',
  deliveredTodayKg: 0,
  deliveredWeekKg: 500,
  deliveredMonthKg: 2000,
  totalDeliveredKg: 5000,
  consumedTodayKg: 30,
  consumedWeekKg: 180,
  consumedMonthKg: 720,
  totalConsumedKg: 3000,
  availableKg: 2000,
  availableBags: 80,
};

/** 14-day feed series with a sharp drop on the last day (triggers the alert). */
export const dailyFeed = Array.from({ length: 14 }, (_, i) => ({
  date: `2026-07-${String(i + 1).padStart(2, '0')}`,
  feedKg: i === 13 ? 20 : 100,
}));

export const globalSearch = {
  users: [{ id: 'u-admin', type: 'USER', title: 'Ada Admin', subtitle: 'EMP-ADMIN' }],
  sites: [{ id: 'site-1', type: 'SITE', title: 'North Farm', subtitle: 'S-001' }],
  ponds: [{ id: 'pond-1', type: 'POND', title: 'Pond One', subtitle: 'P-01' }],
  feedEntries: [],
  medicineEntries: [],
  harvests: [],
  notifications: [],
};

export const medicinePhotos = [
  {
    id: 'mp-1',
    medicineEntryId: 'med-1',
    fileName: 'scan.png',
    filePath: 'https://example.test/scan.png',
    contentType: 'image/png',
    fileSize: 1234,
    uploadedBy: 'EMP-ADMIN',
    uploadedAt: '2026-07-18T09:00:00',
  },
];

export const siteDeliveries = [
  {
    id: 'sd-1',
    siteId: 'site-1',
    siteCode: 'S-001',
    siteName: 'North Farm',
    numberOfBags: 20,
    bagWeightKg: 25,
    totalKg: 500,
    remarks: null,
    status: 'ACTIVE',
  },
];

export const receipts = [
  {
    id: 'rc-1',
    siteDeliveryId: 'sd-1',
    photoPath: '/receipts/rc-1.jpg',
    remarks: null,
    status: 'ACTIVE',
    uploadedByEmployeeCode: 'EMP-DRIVE',
    uploadedAt: '2026-07-18T08:30:00',
  },
];

export const siteFeedAnalytics = {
  siteId: 'site-1',
  siteCode: 'S-001',
  siteName: 'North Farm',
  todayFeedKg: 120,
  weekFeedKg: 700,
  monthFeedKg: 2800,
  todayFeedEntries: 6,
  weekFeedEntries: 40,
  monthFeedEntries: 160,
  pondsFedToday: 3,
  pondsFedWeek: 4,
  pondsFedMonth: 4,
};

export const siteHarvestAnalytics = {
  siteId: 'site-1',
  siteCode: 'S-001',
  siteName: 'North Farm',
  harvestCount: 5,
  todayHarvestKg: 300,
  weekHarvestKg: 900,
  monthHarvestKg: 3600,
  todayRevenue: 45000,
  weekRevenue: 135000,
  monthRevenue: 540000,
};

export const monthlyChart = Array.from({ length: 12 }, (_, i) => ({
  month: i + 1,
  value: (i + 1) * 100,
}));

export const feedInventoryList = [
  {
    id: 'inv-1',
    siteId: 'site-1',
    siteCode: 'S-001',
    siteName: 'North Farm',
    totalReceivedKg: 5000,
    totalConsumedKg: 3000,
    availableKg: 2000,
  },
  {
    id: 'inv-2',
    siteId: 'site-2',
    siteCode: 'S-002',
    siteName: 'South Farm',
    totalReceivedKg: 5000,
    totalConsumedKg: 4600,
    availableKg: 400, // < 20% → low stock
  },
];

export const harvestHistory: HarvestHistoryResponse[] = [
  {
    harvestId: 'h-1',
    cycleNumber: 3,
    harvestDate: '2026-06-30',
    harvestQuantityKg: 250,
    buyerName: 'Buyer Co',
    sellingPricePerKg: 152,
    totalAmount: 38000,
    status: 'ACTIVE',
  },
];
