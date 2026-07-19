import { get, patch, post } from './client';
import { ENDPOINTS } from '@/constants/endpoints';
import type {
  FeedDeliveryResponse,
  HomeDashboardResponse,
  LoginRequest,
  LoginResponse,
  NotificationResponse,
  PondDashboardResponse,
  PondResponse,
  SiteResponse,
  UserResponse,
} from './types';

/** Thin, typed wrappers over the JALA backend REST endpoints. */
export const api = {
  auth: {
    login: (body: LoginRequest) =>
      post<LoginResponse>(ENDPOINTS.auth.login, body),
    me: () => get<UserResponse>(ENDPOINTS.auth.me),
  },

  sites: {
    list: () => get<SiteResponse[]>(ENDPOINTS.sites.base),
    byId: (id: string) => get<SiteResponse>(ENDPOINTS.sites.byId(id)),
  },

  ponds: {
    list: () => get<PondResponse[]>(ENDPOINTS.ponds.base),
    bySite: (siteId: string) =>
      get<PondResponse[]>(ENDPOINTS.ponds.bySite(siteId)),
    byId: (id: string) => get<PondResponse>(ENDPOINTS.ponds.byId(id)),
  },

  dashboard: {
    home: (siteId: string) =>
      get<HomeDashboardResponse>(ENDPOINTS.dashboard.home(siteId)),
    pond: (pondId: string) =>
      get<PondDashboardResponse>(ENDPOINTS.dashboard.pond(pondId)),
  },

  notifications: {
    list: () => get<NotificationResponse>(ENDPOINTS.notifications.base),
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
  },

  users: {
    list: () => get<UserResponse[]>(ENDPOINTS.users.base),
  },
};
