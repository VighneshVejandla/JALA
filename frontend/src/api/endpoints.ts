import { get, patch, post } from './client';
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
    login: (body: LoginRequest) => post<LoginResponse>('/auth/login', body),
    me: () => get<UserResponse>('/auth/me'),
  },

  sites: {
    list: () => get<SiteResponse[]>('/sites'),
    byId: (id: string) => get<SiteResponse>(`/sites/${id}`),
  },

  ponds: {
    list: () => get<PondResponse[]>('/ponds'),
    bySite: (siteId: string) => get<PondResponse[]>(`/ponds/site/${siteId}`),
    byId: (id: string) => get<PondResponse>(`/ponds/${id}`),
  },

  dashboard: {
    home: (siteId: string) =>
      get<HomeDashboardResponse>(`/dashboard/home/${siteId}`),
    pond: (pondId: string) =>
      get<PondDashboardResponse>(`/dashboard/${pondId}`),
  },

  notifications: {
    list: () => get<NotificationResponse[]>('/notifications'),
    unreadCount: () => get<number>('/notifications/unread-count'),
    markRead: (id: string) =>
      patch<NotificationResponse>(`/notifications/${id}/read`),
  },

  feedDeliveries: {
    list: () => get<FeedDeliveryResponse[]>('/feed-deliveries'),
    byId: (id: string) => get<FeedDeliveryResponse>(`/feed-deliveries/${id}`),
    create: (body: { remarks?: string }) =>
      post<FeedDeliveryResponse>('/feed-deliveries', body),
  },

  users: {
    list: () => get<UserResponse[]>('/users'),
  },
};
