import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { api } from './endpoints';
import type {
  AddSiteDeliveryRequest,
  CreateFeedEntryRequest,
  CreateFeedScheduleRequest,
  CreateMedicineRequest,
  CreatePondCycleRequest,
  CreatePondRequest,
  CreateSiteRequest,
  CreateUserRequest,
  ChangePasswordRequest,
  ResetPasswordRequest,
  UpdateFeedEntryRequest,
  UpdateHarvestRequest,
  UpdatePondRequest,
  UpdateProfileRequest,
  UpdateUserRequest,
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

export function useMarkAllNotificationsRead() {
  const qc = useQueryClient();
  return useMutation({
    // No bulk endpoint on the backend, so mark each unread notification read.
    mutationFn: async (ids: string[]) => {
      await Promise.all(ids.map((id) => api.notifications.markRead(id)));
    },
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

export function useDelivery(id: string | null) {
  return useQuery({
    queryKey: id ? ['feed-deliveries', id] : ['feed-deliveries', 'none'],
    queryFn: () => api.feedDeliveries.byId(id as string),
    enabled: !!id,
  });
}

export function useDeliverySites(deliveryId: string | null) {
  return useQuery({
    queryKey: deliveryId
      ? ['feed-deliveries', deliveryId, 'sites']
      : ['feed-deliveries', 'sites', 'none'],
    queryFn: () => api.feedDeliveries.sites(deliveryId as string),
    enabled: !!deliveryId,
  });
}

export function useAddSiteDelivery(deliveryId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: AddSiteDeliveryRequest) =>
      api.feedDeliveries.addSite(deliveryId, body),
    onSuccess: () =>
      qc.invalidateQueries({
        queryKey: ['feed-deliveries', deliveryId, 'sites'],
      }),
  });
}

export function useDeliveryReceipts(siteDeliveryId: string | null) {
  return useQuery({
    queryKey: siteDeliveryId
      ? ['receipts', siteDeliveryId]
      : ['receipts', 'none'],
    queryFn: () =>
      api.siteDeliveryReceipts.list(siteDeliveryId as string),
    enabled: !!siteDeliveryId,
  });
}

export function useUploadReceipt(siteDeliveryId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (form: FormData) => api.siteDeliveryReceipts.upload(form),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: ['receipts', siteDeliveryId] }),
  });
}

export function useSetSiteActive() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      active ? api.sites.activate(id) : api.sites.deactivate(id),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: qk.sites });
    },
  });
}

export function useSetPondActive(siteId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      active ? api.ponds.activate(id) : api.ponds.deactivate(id),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: qk.pondsBySite(siteId) }),
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

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateUserRequest }) =>
      api.users.update(id, body),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.users }),
  });
}

export function useUserSites(userId: string | null) {
  return useQuery({
    queryKey: userId ? ['users', userId, 'sites'] : ['users', 'sites', 'none'],
    queryFn: () => api.users.sites(userId as string),
    enabled: !!userId,
  });
}

export function useAssignSite(userId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ siteId, assign }: { siteId: string; assign: boolean }) =>
      assign
        ? api.users.addSite(userId, siteId)
        : api.users.removeSite(userId, siteId),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: ['users', userId, 'sites'] }),
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

export function useUpdateFeedEntry(cycleId: string, date: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateFeedEntryRequest }) =>
      api.feedEntries.update(id, body),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: qk.feedEntries(cycleId, date) }),
  });
}

export function useCancelFeedEntry(cycleId: string, date: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      api.feedEntries.cancel(id, { reason }),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: qk.feedEntries(cycleId, date) }),
  });
}

export function useCancelMedicine(cycleId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      api.medicines.cancel(id, { reason }),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.medicines(cycleId) }),
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

export function useMedicinePhotos(medicineEntryId: string | null) {
  return useQuery({
    queryKey: medicineEntryId
      ? ['medicine-photos', medicineEntryId]
      : ['medicine-photos', 'none'],
    queryFn: () => api.medicinePhotos.list(medicineEntryId as string),
    enabled: !!medicineEntryId,
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

export function useSiteFeedDaily(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? ['analytics', 'feed-daily', siteId] : ['feed-daily', 'none'],
    queryFn: () => api.analytics.feedSiteDaily(siteId as string),
    enabled: !!siteId,
  });
}

export function useSiteFeedAnalytics(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? ['analytics', 'feed', 'site', siteId] : ['analytics', 'feed', 'none'],
    queryFn: () => api.analytics.feedSite(siteId as string),
    enabled: !!siteId,
  });
}

export function useSiteHarvestAnalytics(siteId: string | null) {
  return useQuery({
    queryKey: siteId
      ? ['analytics', 'harvest', 'site', siteId]
      : ['analytics', 'harvest', 'none'],
    queryFn: () => api.analytics.harvestSite(siteId as string),
    enabled: !!siteId,
  });
}

export function useRevenueChart(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? ['chart', 'revenue', siteId] : ['chart', 'none'],
    queryFn: () => api.reports.revenueChart(siteId as string),
    enabled: !!siteId,
  });
}

export function useFeedChart(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? ['chart', 'feed', siteId] : ['chart', 'none'],
    queryFn: () => api.reports.feedChart(siteId as string),
    enabled: !!siteId,
  });
}

export function useHarvestChart(siteId: string | null) {
  return useQuery({
    queryKey: siteId ? ['chart', 'harvest', siteId] : ['chart', 'none'],
    queryFn: () => api.reports.harvestChart(siteId as string),
    enabled: !!siteId,
  });
}

export function useFeedInventoryList() {
  return useQuery({
    queryKey: ['feed-inventory'],
    queryFn: api.feedInventory.list,
  });
}

export function useUpdateProfile() {
  return useMutation({
    mutationFn: (body: UpdateProfileRequest) => api.auth.updateProfile(body),
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: (body: ChangePasswordRequest) => api.auth.changePassword(body),
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: ResetPasswordRequest }) =>
      api.users.resetPassword(id, body),
  });
}

export function useUpdatePond(siteId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdatePondRequest }) =>
      api.ponds.update(id, body),
    onSuccess: (_d, vars) => {
      void qc.invalidateQueries({ queryKey: qk.pondsBySite(siteId) });
      void qc.invalidateQueries({ queryKey: ['ponds', vars.id] });
    },
  });
}

export function useUpdateHarvest(pondId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateHarvestRequest }) =>
      api.harvests.update(id, body),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: qk.history(pondId, 'harvests') }),
  });
}

export function useCancelHarvest(pondId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      api.harvests.cancel(id, { cancellationReason: reason }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: qk.history(pondId, 'harvests') });
      void qc.invalidateQueries({ queryKey: qk.cyclesByPond(pondId) });
      void qc.invalidateQueries({ queryKey: qk.activeCycle(pondId) });
    },
  });
}

export function useUndoHarvestCycle(pondId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (cycleId: string) => api.pondCycles.undoHarvest(cycleId),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: qk.activeCycle(pondId) });
      void qc.invalidateQueries({ queryKey: qk.cyclesByPond(pondId) });
      void qc.invalidateQueries({ queryKey: qk.pond(pondId) });
    },
  });
}

export function useSearch(keyword: string) {
  const trimmed = keyword.trim();
  return useQuery({
    queryKey: ['search', trimmed],
    queryFn: () => api.search.query(trimmed),
    enabled: trimmed.length >= 2,
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
