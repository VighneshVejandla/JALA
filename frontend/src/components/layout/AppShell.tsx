import { Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { NAV_BY_EXPERIENCE } from './nav';
import { BottomNav } from './BottomNav';
import { SideNav } from './SideNav';
import { TopBar } from './TopBar';

/** Resolve a screen title from the active nav item (falls back to "JALA"). */
function useTitle(items: { to: string; label: string; end?: boolean }[]) {
  const { pathname } = useLocation();
  const match = items
    .filter((i) => (i.end ? pathname === i.to : pathname.startsWith(i.to)))
    .sort((a, b) => b.to.length - a.to.length)[0];
  return match?.label ?? 'JALA';
}

export function AppShell() {
  const { experience } = useAuth();
  const items = experience ? NAV_BY_EXPERIENCE[experience] : [];
  const title = useTitle(items);

  return (
    <div className="flex min-h-svh w-full bg-background">
      <SideNav items={items} />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopBar title={title} />
        <main
          className="mx-auto w-full max-w-3xl flex-1 px-4 pb-24 pt-4 lg:pb-8"
        >
          <Outlet />
        </main>
        <BottomNav items={items} />
      </div>
    </div>
  );
}
