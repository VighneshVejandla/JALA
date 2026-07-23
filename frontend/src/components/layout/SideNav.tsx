import { NavLink } from 'react-router-dom';
import { Droplet } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { NavItem } from './nav';

/** Desktop sidebar (>= lg). Mirrors the bottom nav items. */
export function SideNav({ items }: { items: NavItem[] }) {
  return (
    <aside className="sticky top-0 hidden h-svh w-60 shrink-0 flex-col border-r border-sidebar-border bg-sidebar text-sidebar-foreground lg:flex">
      <div className="flex items-center gap-2 px-5 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
          <Droplet className="h-5 w-5" />
        </div>
        <div className="leading-tight">
          <p className="text-sm font-semibold">JALA</p>
          <p className="text-xs text-sidebar-foreground/70">Aqua Management</p>
        </div>
      </div>
      <nav className="flex-1 space-y-1 px-3 py-2" aria-label="Primary">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                  : 'text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
              )
            }
          >
            <item.icon className="h-5 w-5" />
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
