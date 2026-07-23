/**
 * Shared API types mirroring the JALA Spring Boot backend DTOs.
 * Every backend response is wrapped in ApiResponse<T>; the axios client
 * unwraps `.data` so callers here work with the inner payload directly.
 */

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

/** Raw backend roles. The UI folds these into three experiences (see auth/roles). */
export type BackendRole =
  | 'ADMIN'
  | 'MANAGER'
  | 'SUPERVISOR'
  | 'WORKER'
  | 'DRIVER';

export interface LoginRequest {
  employeeCode: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  employeeCode: string;
  fullName: string;
  role: BackendRole;
}

export interface UserResponse {
  id: string;
  employeeCode: string;
  fullName: string;
  email: string | null;
  phone: string | null;
  role: BackendRole;
  isActive: boolean;
}

export interface SiteResponse {
  id: string;
  siteCode: string;
  siteName: string;
  ownerName: string | null;
  location: string | null;
  totalAcres: number | null;
  isActive: boolean;
}

export interface PondResponse {
  id: string;
  siteId: string;
  siteName: string;
  pondCode: string;
  pondName: string;
  pondAcres: number | null;
  isActive: boolean;
}

export interface HomeDashboardResponse {
  siteId: string;
  siteCode: string;
  siteName: string;
  totalPonds: number;
  activeCycles: number;
  todayFeedKg: number;
  availableFeedKg: number;
  todayHarvestKg: number;
  todayRevenue: number;
  unreadNotifications: number;
  lowInventory: boolean;
}

export interface PondDashboardResponse {
  pondId: string;
  pondCode: string;
  pondName: string;
  siteId: string;
  siteCode: string;
  siteName: string;
  activeCycleId: string | null;
  cycleNumber: number | null;
  cycleStatus: string | null;
  stockingCompleted: boolean | null;
  species: string | null;
  stockingDate: string | null;
  daysSinceStocking: number | null;
  shrimpCount: number | null;
  todayFeedKg: number | null;
  totalFeedKg: number | null;
  todayFeedEntries: number | null;
  medicineEntryCount: number | null;
  totalMedicineQuantity: number | null;
  lastMedicineDate: string | null;
  medicinePhotoCount: number | null;
  harvestCount: number | null;
  lastHarvestDate: string | null;
  lastHarvestQuantityKg: number | null;
  lastHarvestAmount: number | null;
  lastBuyerName: string | null;
}

export type NotificationStatus = 'UNREAD' | 'READ';

export interface NotificationResponse {
  id: string;
  type: string;
  title: string;
  message: string;
  siteId: string | null;
  pondId: string | null;
  status: NotificationStatus;
  createdAt: string;
  readAt: string | null;
}

export interface FeedDeliveryResponse {
  id: string;
  deliveredBy: string | null;
  deliveredAt: string | null;
  remarks: string | null;
  status: string;
}

export interface SiteDeliveryResponse {
  id: string;
  siteId: string;
  siteCode: string;
  siteName: string;
  numberOfBags: number | null;
  bagWeightKg: number | null;
  totalKg: number | null;
  remarks: string | null;
  status: string;
}

export interface AddSiteDeliveryRequest {
  siteId: string;
  numberOfBags: number;
  remarks?: string;
}

export interface SiteDeliveryReceiptResponse {
  id: string;
  siteDeliveryId: string;
  photoPath: string;
  remarks: string | null;
  status: string;
  uploadedByEmployeeCode: string | null;
  uploadedAt: string;
}

/** Spring Data Page envelope, used by list endpoints that paginate. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface NotificationsResponse {
  unreadCount: number;
  notifications: NotificationResponse[];
}

// ---------------------------------------------------------------------------
// Enums (serialized by name)
// ---------------------------------------------------------------------------

export type ShrimpSpecies = 'VANNAMEI' | 'TIGER';
export type PondCycleStatus = 'ACTIVE' | 'HARVESTED';
export type HarvestStatus = 'ACTIVE' | 'CANCELLED';
export type FeedEntryStatus = 'ACTIVE' | 'CANCELLED';
export type MedicineUnit = 'KG' | 'GM' | 'LTR' | 'ML';
export type FeedSize =
  | 'ONE'
  | 'TWO'
  | 'TWO_S'
  | 'THREE'
  | 'THREE_S'
  | 'FOUR'
  | 'FOUR_S'
  | 'FIVE';

/** Human-facing labels for the feed-size codes. */
export const FEED_SIZE_LABELS: Record<FeedSize, string> = {
  ONE: '1',
  TWO: '2',
  TWO_S: '2S',
  THREE: '3',
  THREE_S: '3S',
  FOUR: '4',
  FOUR_S: '4S',
  FIVE: '5',
};

// ---------------------------------------------------------------------------
// Roles
// ---------------------------------------------------------------------------

export interface RoleResponse {
  id: string;
  name: string;
  description: string | null;
}

// ---------------------------------------------------------------------------
// Users (create / update)
// ---------------------------------------------------------------------------

export interface CreateUserRequest {
  roleId: string;
  employeeCode: string;
  fullName: string;
  email?: string;
  phone?: string;
  password: string;
}

export interface UpdateUserRequest {
  roleId?: string;
  fullName?: string;
  email?: string;
  phone?: string;
  isActive?: boolean;
}

// ---------------------------------------------------------------------------
// Sites / Ponds (create)
// ---------------------------------------------------------------------------

export interface CreateSiteRequest {
  siteCode: string;
  siteName: string;
  ownerName: string;
  location: string;
  totalAcres: number;
}

export interface CreatePondRequest {
  siteId: string;
  pondCode: string;
  pondName: string;
  pondAcres: number;
}

export interface UpdatePondRequest {
  pondCode?: string;
  pondName?: string;
  pondAcres?: number;
  isActive?: boolean;
}

export interface UpdateProfileRequest {
  fullName?: string;
  email?: string;
  phone?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

export interface UpdateHarvestRequest {
  harvestDate?: string;
  harvestQuantityKg?: number;
  buyerName?: string;
  sellingPricePerKg?: number;
  vehicleNumber?: string;
  remarks?: string;
}

// ---------------------------------------------------------------------------
// Pond cycles
// ---------------------------------------------------------------------------

export interface PondCycleResponse {
  id: string;
  pondId: string;
  pondName: string;
  cycleNumber: number;
  species: ShrimpSpecies;
  stockingDate: string;
  shrimpCount: number;
  stockingCompleted: boolean;
  status: PondCycleStatus;
}

export interface CreatePondCycleRequest {
  pondId: string;
  species: ShrimpSpecies;
  stockingDate: string;
  shrimpCount: number;
}

// ---------------------------------------------------------------------------
// Feed schedules (sessions) + feed entries (amounts)
// ---------------------------------------------------------------------------

export interface FeedScheduleResponse {
  id: string;
  pondCycleId: string;
  sessionNumber: number;
  feedingTime: string; // HH:mm[:ss]
  isActive: boolean;
}

export interface CreateFeedScheduleRequest {
  pondCycleId: string;
  feedingTimes: string[]; // HH:mm
}

export interface FeedEntryResponse {
  id: string;
  pondCycleId: string;
  feedScheduleId: string;
  sessionNumber: number;
  feedDate: string;
  feedSize: FeedSize;
  feedQuantityKg: number;
  remarks: string | null;
  createdBy: string | null;
}

export interface CreateFeedEntryRequest {
  pondCycleId: string;
  feedScheduleId: string;
  feedDate: string;
  feedSize: FeedSize;
  feedQuantityKg: number;
  remarks?: string;
}

export interface UpdateFeedEntryRequest {
  feedSize?: FeedSize;
  feedQuantityKg?: number;
  remarks?: string;
}

// ---------------------------------------------------------------------------
// Medicines + photos
// ---------------------------------------------------------------------------

export interface MedicineResponse {
  id: string;
  pondCycleId: string;
  quantity: number;
  unit: MedicineUnit;
  remarks: string | null;
  status: string;
  createdBy: string | null;
  createdAt: string;
}

export interface CreateMedicineRequest {
  pondCycleId: string;
  quantity: number;
  unit: MedicineUnit;
  remarks?: string;
}

export interface MedicinePhotoResponse {
  id: string;
  medicineEntryId: string;
  fileName: string;
  filePath: string;
  contentType: string;
  fileSize: number;
  uploadedBy: string | null;
  uploadedAt: string;
}

// ---------------------------------------------------------------------------
// Harvests
// ---------------------------------------------------------------------------

export interface HarvestResponse {
  id: string;
  pondCycleId: string;
  harvestDate: string;
  harvestQuantityKg: number;
  quantityDisplay: string | null;
  billPhotoPath: string | null;
  buyerName: string | null;
  sellingPricePerKg: number | null;
  totalAmount: number | null;
  vehicleNumber: string | null;
  remarks: string | null;
  status: HarvestStatus;
  uploadedByEmployeeCode: string | null;
  uploadedAt: string;
  cancelledByEmployeeCode: string | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
}

// ---------------------------------------------------------------------------
// History
// ---------------------------------------------------------------------------

export interface PondCycleHistoryResponse {
  cycleId: string;
  cycleNumber: number;
  currentCycle: boolean;
  status: PondCycleStatus;
  species: ShrimpSpecies;
  stockingDate: string;
  shrimpCount: number;
  harvestDate: string | null;
  totalFeedEntries: number;
  totalMedicineEntries: number;
  totalHarvests: number;
}

export interface HarvestHistoryResponse {
  harvestId: string;
  cycleNumber: number;
  harvestDate: string;
  harvestQuantityKg: number;
  buyerName: string | null;
  sellingPricePerKg: number | null;
  totalAmount: number | null;
  billPhotoPath: string | null;
  status: HarvestStatus;
}

export interface FeedHistoryResponse {
  feedEntryId: string;
  cycleNumber: number;
  sessionNumber: number;
  feedDate: string;
  feedSize: FeedSize;
  feedQuantityKg: number;
  remarks: string | null;
  status: FeedEntryStatus;
  createdBy: string | null;
}

export interface MedicineHistoryPhotoResponse {
  photoId: string;
  fileName: string;
  filePath: string;
}

export interface MedicineHistoryResponse {
  medicineId: string;
  cycleNumber: number;
  quantity: number;
  unit: MedicineUnit;
  remarks: string | null;
  status: string;
  createdBy: string | null;
  createdAt: string;
  photos: MedicineHistoryPhotoResponse[];
}

export interface PondTimelineItemResponse {
  eventTime: string;
  eventType: string;
  title: string;
  description: string | null;
  cycleNumber: number | null;
  referenceId: string | null;
  referenceType: string | null;
}

export interface PondTimelineResponse {
  pondId: string;
  pondCode: string;
  pondName: string;
  timeline: PondTimelineItemResponse[];
}

// ---------------------------------------------------------------------------
// Reports / exports
// ---------------------------------------------------------------------------

export interface ReportFilterRequest {
  siteId: string;
  pondId?: string;
  fromDate: string;
  toDate: string;
}

// ---------------------------------------------------------------------------
// Analytics
// ---------------------------------------------------------------------------

export interface FeedAnalyticsResponse {
  pondId: string;
  pondCode: string;
  pondName: string;
  todayFeedKg: number;
  weekFeedKg: number;
  monthFeedKg: number;
  todayFeedEntries: number;
  weekFeedEntries: number;
  monthFeedEntries: number;
}

export interface PondHarvestAnalyticsResponse {
  pondId: string;
  pondCode: string;
  pondName: string;
  harvestCount: number;
  totalHarvestKg: number;
  averageHarvestKg: number;
  totalRevenue: number;
  lastHarvestDate: string | null;
  lastHarvestQuantityKg: number | null;
  lastHarvestRevenue: number | null;
  lastBuyer: string | null;
}

export interface InventoryAnalyticsResponse {
  siteId: string;
  siteCode: string;
  siteName: string;
  deliveredTodayKg: number;
  deliveredWeekKg: number;
  deliveredMonthKg: number;
  totalDeliveredKg: number;
  consumedTodayKg: number;
  consumedWeekKg: number;
  consumedMonthKg: number;
  totalConsumedKg: number;
  availableKg: number;
  availableBags: number;
}

export interface FeedInventoryResponse {
  id: string;
  siteId: string;
  siteCode: string;
  siteName: string;
  totalReceivedKg: number;
  totalConsumedKg: number;
  availableKg: number;
}

export interface SiteFeedAnalyticsResponse {
  siteId: string;
  siteCode: string;
  siteName: string;
  todayFeedKg: number;
  weekFeedKg: number;
  monthFeedKg: number;
  todayFeedEntries: number;
  weekFeedEntries: number;
  monthFeedEntries: number;
  pondsFedToday: number;
  pondsFedWeek: number;
  pondsFedMonth: number;
}

export interface SiteHarvestAnalyticsResponse {
  siteId: string;
  siteCode: string;
  siteName: string;
  harvestCount: number;
  todayHarvestKg: number;
  weekHarvestKg: number;
  monthHarvestKg: number;
  todayRevenue: number;
  weekRevenue: number;
  monthRevenue: number;
}

/** A single month bucket in a 12-month report chart series. */
export interface MonthlyChartResponse {
  month: number; // 1–12
  value: number;
}

/** One day's total feed for a site (daily trend series). */
export interface DailyFeedResponse {
  date: string;
  feedKg: number;
}

// ---------------------------------------------------------------------------
// Global search
// ---------------------------------------------------------------------------

export interface SearchResultResponse {
  id: string;
  type: string;
  title: string;
  subtitle: string | null;
}

export interface GlobalSearchResponse {
  users: SearchResultResponse[];
  sites: SearchResultResponse[];
  ponds: SearchResultResponse[];
  feedEntries: SearchResultResponse[];
  medicineEntries: SearchResultResponse[];
  harvests: SearchResultResponse[];
  notifications: SearchResultResponse[];
}
