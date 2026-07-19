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
