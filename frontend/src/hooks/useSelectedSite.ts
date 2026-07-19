import { useEffect, useMemo, useState } from 'react';
import { useSites } from '@/api/queries';
import type { SiteResponse } from '@/api/types';

const STORAGE_KEY = 'jala.selectedSiteId';

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

  useEffect(() => {
    if (sites.length === 0) return;
    const stillValid = siteId && sites.some((s) => s.id === siteId);
    if (!stillValid) {
      setSiteId(sites[0].id);
    }
  }, [sites, siteId]);

  const select = (id: string) => {
    setSiteId(id);
    localStorage.setItem(STORAGE_KEY, id);
  };

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
