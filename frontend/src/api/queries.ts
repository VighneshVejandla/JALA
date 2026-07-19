import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { api } from './endpoints';
import type {
  CreateFeedEntryRequest,
  CreateFeedScheduleRequest,
  CreateMedicineRequest,
  CreatePondCycleRequest,
  CreatePondRequest,
  CreateSiteRequest,
  CreateUserRequest,
} from './types';

export const qk = {
  roles: ['roles'] as const,
  sites: ['sites'] as const,
  site: (id: string) => ['sites', id] as const,
  ponds: ['ponds'] as const,
  pondsBySite: (siteId: string) => ['ponds', 'site', siteId] as const,
  home: (siteId: string) => ['dashboard', 'home', siteId] as const,
  pond: (pondId: string) => ['dashboard', 'pond', pondId] as const,
  activeCycle: (pondId: string) => ['pond-cycles', 'active', pondId] as const,
  cyclesByPond: (pondId: string) => ['pond-cycles', 'pond', pondId] as const,
  feedSchedules: (cycleId: string) => ['feed-schedules', cycleId] as const,
  feedEntries: (cycleId: string, date: string) =>
    ['feed-entries', cycleId, date] as const,
  medicines: (cycleId: string) => ['medicines', cycleId] as const,
  harvests: (cycleId: string) => ['harvests', cycleId] as const,
  feedAnalytics: (pondId: string) => ['analytics', 'feed', pondId] as const,
  inventory: (siteId: string) => ['analytics', 'inventory', siteId] as const,
  history: (pondId: string, kind: string) =>
    ['history', pondId, kind] as const,
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

export function useRoles() {
  return useQuery({ queryKey: qk.roles, queryFn: api.roles.list });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateUserRequest) => api.users.create(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.users }),
  });
}

export function useSetUserActive() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      active ? api.users.activate(id) : api.users.deactivate(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.users }),
  });
}

export function useCreateSite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateSiteRequest) => api.sites.create(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.sites }),
  });
}

export function useSite(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? qk.site(siteId) : ['sites', 'none'],
    queryFn: () => api.sites.byId(siteId as string),
    enabled: !!siteId,
  });
}

export function useCreatePond() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreatePondRequest) => api.ponds.create(body),
    onSuccess: (_data, vars) =>
      qc.invalidateQueries({ queryKey: qk.pondsBySite(vars.siteId) }),
  });
}

export function usePond(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? ['ponds', pondId] : ['ponds', 'none'],
    queryFn: () => api.ponds.byId(pondId as string),
    enabled: !!pondId,
  });
}

export function useActiveCycle(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.activeCycle(pondId) : ['pond-cycles', 'active', 'none'],
    queryFn: () => api.pondCycles.active(pondId as string),
    enabled: !!pondId,
    // A pond with no active cycle returns 404 — treat that as "no cycle".
    retry: false,
  });
}

export function useCreateCycle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreatePondCycleRequest) => api.pondCycles.create(body),
    onSuccess: (_data, vars) => {
      void qc.invalidateQueries({ queryKey: qk.activeCycle(vars.pondId) });
      void qc.invalidateQueries({ queryKey: qk.cyclesByPond(vars.pondId) });
    },
  });
}

export function useHarvestCycle(pondId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cycleId: string) => api.pondCycles.harvest(cycleId),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: qk.activeCycle(pondId) });
      void qc.invalidateQueries({ queryKey: qk.cyclesByPond(pondId) });
      void qc.invalidateQueries({ queryKey: qk.pond(pondId) });
    },
  });
}

export function useFeedSchedules(cycleId: string | null) {
  return useQuery({
    queryKey: cycleId ? qk.feedSchedules(cycleId) : ['feed-schedules', 'none'],
    queryFn: () => api.feedSchedules.byCycle(cycleId as string),
    enabled: !!cycleId,
  });
}

export function useCreateFeedSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateFeedScheduleRequest) =>
      api.feedSchedules.create(body),
    onSuccess: (_data, vars) =>
      qc.invalidateQueries({ queryKey: qk.feedSchedules(vars.pondCycleId) }),
  });
}

export function useFeedEntries(cycleId: string | null, date: string) {
  return useQuery({
    queryKey: cycleId ? qk.feedEntries(cycleId, date) : ['feed-entries', 'none'],
    queryFn: () => api.feedEntries.list(cycleId as string, date),
    enabled: !!cycleId,
  });
}

export function useCreateFeedEntry(date: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateFeedEntryRequest) => api.feedEntries.create(body),
    onSuccess: (_data, vars) =>
      qc.invalidateQueries({
        queryKey: qk.feedEntries(vars.pondCycleId, date),
      }),
  });
}

export function useMedicines(cycleId: string | null) {
  return useQuery({
    queryKey: cycleId ? qk.medicines(cycleId) : ['medicines', 'none'],
    queryFn: () => api.medicines.list(cycleId as string),
    enabled: !!cycleId,
  });
}

export function useCreateMedicine() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateMedicineRequest) => api.medicines.create(body),
    onSuccess: (_data, vars) =>
      qc.invalidateQueries({ queryKey: qk.medicines(vars.pondCycleId) }),
  });
}

export function useHarvests(cycleId: string | null) {
  return useQuery({
    queryKey: cycleId ? qk.harvests(cycleId) : ['harvests', 'none'],
    queryFn: () => api.harvests.list(cycleId as string),
    enabled: !!cycleId,
  });
}

export function useCreateHarvest(cycleId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (form: FormData) => api.harvests.create(form),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.harvests(cycleId) }),
  });
}

export function useFeedAnalytics(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.feedAnalytics(pondId) : ['analytics', 'feed', 'none'],
    queryFn: () => api.analytics.feedPond(pondId as string),
    enabled: !!pondId,
  });
}

export function useHarvestAnalytics(pondId: string | null) {
  return useQuery({
    queryKey: pondId
      ? ['analytics', 'harvest', pondId]
      : ['analytics', 'harvest', 'none'],
    queryFn: () => api.analytics.harvestPond(pondId as string),
    enabled: !!pondId,
  });
}

export function useInventory(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? qk.inventory(siteId) : ['analytics', 'inventory', 'none'],
    queryFn: () => api.analytics.inventorySite(siteId as string),
    enabled: !!siteId,
  });
}

export function useCyclesByPond(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.cyclesByPond(pondId) : ['pond-cycles', 'pond', 'none'],
    queryFn: () => api.pondCycles.byPond(pondId as string),
    enabled: !!pondId,
  });
}

export function useHistoryCycles(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.history(pondId, 'cycles') : ['history', 'none'],
    queryFn: () => api.history.cycles(pondId as string),
    enabled: !!pondId,
  });
}

export function useHistoryFeeds(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.history(pondId, 'feeds') : ['history', 'none'],
    queryFn: () => api.history.feeds(pondId as string),
    enabled: !!pondId,
  });
}

export function useHistoryMedicines(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.history(pondId, 'medicines') : ['history', 'none'],
    queryFn: () => api.history.medicines(pondId as string),
    enabled: !!pondId,
  });
}

export function useHistoryHarvests(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.history(pondId, 'harvests') : ['history', 'none'],
    queryFn: () => api.history.harvests(pondId as string),
    enabled: !!pondId,
  });
}

export function useHistoryTimeline(pondId: string | null) {
  return useQuery({
    queryKey: pondId ? qk.history(pondId, 'timeline') : ['history', 'none'],
    queryFn: () => api.history.timeline(pondId as string),
    enabled: !!pondId,
  });
}
