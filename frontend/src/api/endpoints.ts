import { del, get, patch, post, postBlob, postForm } from './client';
import { ENDPOINTS } from '@/constants/endpoints';
import type {
  AddSiteDeliveryRequest,
  CreateFeedEntryRequest,
  CreateFeedScheduleRequest,
  CreateMedicineRequest,
  CreatePondCycleRequest,
  CreatePondRequest,
  CreateSiteRequest,
  CreateUserRequest,
  FeedAnalyticsResponse,
  FeedDeliveryResponse,
  FeedEntryResponse,
  FeedHistoryResponse,
  FeedInventoryResponse,
  FeedScheduleResponse,
  GlobalSearchResponse,
  HarvestHistoryResponse,
  HarvestResponse,
  HomeDashboardResponse,
  InventoryAnalyticsResponse,
  LoginRequest,
  LoginResponse,
  MedicineHistoryResponse,
  MedicineResponse,
  MedicinePhotoResponse,
  MonthlyChartResponse,
  NotificationResponse,
  NotificationsResponse,
  PondCycleHistoryResponse,
  PondCycleResponse,
  PondDashboardResponse,
  PondHarvestAnalyticsResponse,
  PondResponse,
  PondTimelineResponse,
  ReportFilterRequest,
  RoleResponse,
  UpdateFeedEntryRequest,
  SiteDeliveryReceiptResponse,
  SiteDeliveryResponse,
  SiteFeedAnalyticsResponse,
  SiteHarvestAnalyticsResponse,
  SiteResponse,
  UpdateUserRequest,
  UserResponse,
} from './types';

/** Thin, typed wrappers over the JALA backend REST endpoints. */
export const api = {
  auth: {
    login: (body: LoginRequest) =>
      post<LoginResponse>(ENDPOINTS.auth.login, body),
    me: () => get<UserResponse>(ENDPOINTS.auth.me),
  },

  roles: {
    list: () => get<RoleResponse[]>(ENDPOINTS.roles.base),
  },

  users: {
    list: () => get<UserResponse[]>(ENDPOINTS.users.base),
    create: (body: CreateUserRequest) =>
      post<UserResponse>(ENDPOINTS.users.base, body),
    update: (id: string, body: UpdateUserRequest) =>
      patch<UserResponse>(ENDPOINTS.users.byId(id), body),
    activate: (id: string) => patch<void>(ENDPOINTS.users.activate(id)),
    deactivate: (id: string) => patch<void>(ENDPOINTS.users.deactivate(id)),
    sites: (id: string) => get<string[]>(ENDPOINTS.users.sites(id)),
    addSite: (id: string, siteId: string) =>
      post<void>(ENDPOINTS.users.site(id, siteId)),
    removeSite: (id: string, siteId: string) =>
      del<void>(ENDPOINTS.users.site(id, siteId)),
  },

  sites: {
    list: () => get<SiteResponse[]>(ENDPOINTS.sites.base),
    byId: (id: string) => get<SiteResponse>(ENDPOINTS.sites.byId(id)),
    create: (body: CreateSiteRequest) =>
      post<SiteResponse>(ENDPOINTS.sites.base, body),
    activate: (id: string) => patch<void>(ENDPOINTS.sites.activate(id)),
    deactivate: (id: string) => patch<void>(ENDPOINTS.sites.deactivate(id)),
  },

  ponds: {
    list: () => get<PondResponse[]>(ENDPOINTS.ponds.base),
    bySite: (siteId: string) =>
      get<PondResponse[]>(ENDPOINTS.ponds.bySite(siteId)),
    byId: (id: string) => get<PondResponse>(ENDPOINTS.ponds.byId(id)),
    create: (body: CreatePondRequest) =>
      post<PondResponse>(ENDPOINTS.ponds.base, body),
    activate: (id: string) => patch<void>(ENDPOINTS.ponds.activate(id)),
    deactivate: (id: string) => patch<void>(ENDPOINTS.ponds.deactivate(id)),
  },

  pondCycles: {
    active: (pondId: string) =>
      get<PondCycleResponse>(ENDPOINTS.pondCycles.active(pondId)),
    byPond: (pondId: string) =>
      get<PondCycleResponse[]>(ENDPOINTS.pondCycles.byPond(pondId)),
    create: (body: CreatePondCycleRequest) =>
      post<PondCycleResponse>(ENDPOINTS.pondCycles.base, body),
    harvest: (id: string) => patch<void>(ENDPOINTS.pondCycles.harvest(id)),
    undoHarvest: (id: string) =>
      patch<void>(ENDPOINTS.pondCycles.undoHarvest(id)),
  },

  feedSchedules: {
    byCycle: (cycleId: string) =>
      get<FeedScheduleResponse[]>(ENDPOINTS.feedSchedules.byCycle(cycleId)),
    create: (body: CreateFeedScheduleRequest) =>
      post<FeedScheduleResponse[]>(ENDPOINTS.feedSchedules.base, body),
  },

  feedEntries: {
    list: (pondCycleId: string, date: string) =>
      get<FeedEntryResponse[]>(ENDPOINTS.feedEntries.base, {
        pondCycleId,
        date,
      }),
    create: (body: CreateFeedEntryRequest) =>
      post<FeedEntryResponse>(ENDPOINTS.feedEntries.base, body),
    update: (id: string, body: UpdateFeedEntryRequest) =>
      patch<FeedEntryResponse>(ENDPOINTS.feedEntries.byId(id), body),
  },

  medicines: {
    list: (pondCycleId: string) =>
      get<MedicineResponse[]>(ENDPOINTS.medicines.base, { pondCycleId }),
    create: (body: CreateMedicineRequest) =>
      post<MedicineResponse>(ENDPOINTS.medicines.base, body),
  },

  medicinePhotos: {
    list: (medicineEntryId: string) =>
      get<MedicinePhotoResponse[]>(ENDPOINTS.medicinePhotos.base, {
        medicineEntryId,
      }),
    upload: (form: FormData) =>
      postForm<MedicinePhotoResponse>(ENDPOINTS.medicinePhotos.base, form),
  },

  harvests: {
    list: (pondCycleId: string) =>
      get<HarvestResponse[]>(ENDPOINTS.harvests.base, { pondCycleId }),
    create: (form: FormData) =>
      postForm<HarvestResponse>(ENDPOINTS.harvests.base, form),
  },

  history: {
    cycles: (pondId: string) =>
      get<PondCycleHistoryResponse[]>(ENDPOINTS.history.cycles(pondId)),
    harvests: (pondId: string) =>
      get<HarvestHistoryResponse[]>(ENDPOINTS.history.harvests(pondId)),
    feeds: (pondId: string) =>
      get<FeedHistoryResponse[]>(ENDPOINTS.history.feeds(pondId)),
    medicines: (pondId: string) =>
      get<MedicineHistoryResponse[]>(ENDPOINTS.history.medicines(pondId)),
    timeline: (pondId: string) =>
      get<PondTimelineResponse>(ENDPOINTS.history.timeline(pondId)),
  },

  exports: {
    revenueExcel: (body: ReportFilterRequest) =>
      postBlob(ENDPOINTS.exports.revenueExcel, body),
    revenuePdf: (body: ReportFilterRequest) =>
      postBlob(ENDPOINTS.exports.revenuePdf, body),
    feedExcel: (body: ReportFilterRequest) =>
      postBlob(ENDPOINTS.exports.feedExcel, body),
    feedPdf: (body: ReportFilterRequest) =>
      postBlob(ENDPOINTS.exports.feedPdf, body),
    medicineExcel: (body: ReportFilterRequest) =>
      postBlob(ENDPOINTS.exports.medicineExcel, body),
    medicinePdf: (body: ReportFilterRequest) =>
      postBlob(ENDPOINTS.exports.medicinePdf, body),
  },

  analytics: {
    feedPond: (pondId: string) =>
      get<FeedAnalyticsResponse>(ENDPOINTS.analytics.feedPond(pondId)),
    feedSite: (siteId: string) =>
      get<SiteFeedAnalyticsResponse>(ENDPOINTS.analytics.feedSite(siteId)),
    harvestPond: (pondId: string) =>
      get<PondHarvestAnalyticsResponse>(ENDPOINTS.analytics.harvestPond(pondId)),
    harvestSite: (siteId: string) =>
      get<SiteHarvestAnalyticsResponse>(ENDPOINTS.analytics.harvestSite(siteId)),
    inventorySite: (siteId: string) =>
      get<InventoryAnalyticsResponse>(ENDPOINTS.analytics.inventorySite(siteId)),
  },

  reports: {
    revenueChart: (siteId: string) =>
      get<MonthlyChartResponse[]>(ENDPOINTS.reports.revenueChart(siteId)),
    feedChart: (siteId: string) =>
      get<MonthlyChartResponse[]>(ENDPOINTS.reports.feedChart(siteId)),
    harvestChart: (siteId: string) =>
      get<MonthlyChartResponse[]>(ENDPOINTS.reports.harvestChart(siteId)),
  },

  feedInventory: {
    list: () => get<FeedInventoryResponse[]>(ENDPOINTS.feedInventory.base),
    bySite: (siteId: string) =>
      get<FeedInventoryResponse>(ENDPOINTS.feedInventory.bySite(siteId)),
  },

  dashboard: {
    home: (siteId: string) =>
      get<HomeDashboardResponse>(ENDPOINTS.dashboard.home(siteId)),
    pond: (pondId: string) =>
      get<PondDashboardResponse>(ENDPOINTS.dashboard.pond(pondId)),
  },

  notifications: {
    list: () => get<NotificationsResponse>(ENDPOINTS.notifications.base),
    unreadCount: () => get<number>(ENDPOINTS.notifications.unreadCount),
    markRead: (id: string) =>
      patch<NotificationResponse>(ENDPOINTS.notifications.read(id)),
  },

  feedDeliveries: {
    list: () => get<FeedDeliveryResponse[]>(ENDPOINTS.feedDeliveries.base),
    byId: (id: string) =>
      get<FeedDeliveryResponse>(ENDPOINTS.feedDeliveries.byId(id)),
    create: (body: { remarks?: string }) =>
      post<FeedDeliveryResponse>(ENDPOINTS.feedDeliveries.base, body),
    sites: (deliveryId: string) =>
      get<SiteDeliveryResponse[]>(ENDPOINTS.feedDeliveries.sites(deliveryId)),
    addSite: (deliveryId: string, body: AddSiteDeliveryRequest) =>
      post<SiteDeliveryResponse>(ENDPOINTS.feedDeliveries.sites(deliveryId), body),
  },

  siteDeliveryReceipts: {
    list: (siteDeliveryId: string) =>
      get<SiteDeliveryReceiptResponse[]>(ENDPOINTS.siteDeliveryReceipts.base, {
        siteDeliveryId,
      }),
    upload: (form: FormData) =>
      postForm<SiteDeliveryReceiptResponse>(
        ENDPOINTS.siteDeliveryReceipts.base,
        form,
      ),
  },

  search: {
    query: (keyword: string) =>
      get<GlobalSearchResponse>(ENDPOINTS.search.base, { keyword }),
  },
};
