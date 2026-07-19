import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { api } from './endpoints';

export const qk = {
  sites: ['sites'] as const,
  ponds: ['ponds'] as const,
  pondsBySite: (siteId: string) => ['ponds', 'site', siteId] as const,
  home: (siteId: string) => ['dashboard', 'home', siteId] as const,
  pond: (pondId: string) => ['dashboard', 'pond', pondId] as const,
  notifications: ['notifications'] as const,
  unreadCount: ['notifications', 'unread-count'] as const,
  deliveries: ['feed-deliveries'] as const,
  users: ['users'] as const,
};

export function useSites() {
  return useQuery({ queryKey: qk.sites, queryFn: api.sites.list });
}

export function usePondsBySite(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? qk.pondsBySite(siteId) : qk.ponds,
    queryFn: () => api.ponds.bySite(siteId as string),
    enabled: !!siteId,
  });
}

export function useHomeDashboard(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? qk.home(siteId) : ['dashboard', 'home', 'none'],
    queryFn: () => api.dashboard.home(siteId as string),
    enabled: !!siteId,
  });
}

export function usePondDashboard(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.pond(pondId) : ['dashboard', 'pond', 'none'],
    queryFn: () => api.dashboard.pond(pondId as string),
    enabled: !!pondId,
  });
}

export function useNotifications() {
  return useQuery({
    queryKey: qk.notifications,
    queryFn: api.notifications.list,
  });
}

export function useMarkNotificationRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.notifications.markRead(id),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: qk.notifications });
      void qc.invalidateQueries({ queryKey: qk.unreadCount });
    },
  });
}

export function useDeliveries() {
  return useQuery({
    queryKey: qk.deliveries,
    queryFn: api.feedDeliveries.list,
  });
}

export function useCreateDelivery() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: { remarks?: string }) =>
      api.feedDeliveries.create(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.deliveries }),
  });
}

export function useUsers() {
  return useQuery({ queryKey: qk.users, queryFn: api.users.list });
}
