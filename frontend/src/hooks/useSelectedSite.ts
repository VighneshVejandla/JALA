import { useCallback, useEffect, useMemo, useState } from 'react';
import { useSites } from '@/api/queries';
import { STORAGE_KEYS } from '@/constants/storage';
import type { SiteResponse } from '@/api/types';

const STORAGE_KEY = STORAGE_KEYS.selectedSiteId;

/** Forget the persisted site (call on logout so the next user starts fresh). */
export function clearSelectedSite() {
  localStorage.removeItem(STORAGE_KEY);
}

/**
 * Tracks the site the user is currently viewing. Persists the choice and
 * defaults to the first accessible site once the list loads.
 */
export function useSelectedSite() {
  const query = useSites();
  const sites = useMemo<SiteResponse[]>(() => query.data ?? [], [query.data]);
  const [siteId, setSiteId] = useState<string | null>(
    () => localStorage.getItem(STORAGE_KEY),
  );

  const select = useCallback((id: string) => {
    setSiteId(id);
    localStorage.setItem(STORAGE_KEY, id);
  }, []);

  // Once sites load, keep the selection valid — default to the first site
  // (and persist it) whenever the current id is missing or no longer accessible.
  useEffect(() => {
    if (sites.length === 0) return;
    const stillValid = siteId && sites.some((s) => s.id === siteId);
    if (!stillValid) {
      select(sites[0].id);
    }
  }, [sites, siteId, select]);

  const selectedSite = sites.find((s) => s.id === siteId) ?? null;

  return {
    sites,
    siteId,
    selectedSite,
    select,
    isLoading: query.isLoading,
    isError: query.isError,
    refetch: query.refetch,
  };
}
