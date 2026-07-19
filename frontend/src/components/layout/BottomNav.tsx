import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/utils';
import type { NavItem } from './nav';

/** Mobile-first bottom tab bar; hidden on large screens (sidebar takes over). */
export function BottomNav({ items }: { items: NavItem[] }) {
  return (
    <nav
      className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/80 lg:hidden"
      style={{ paddingBottom: 'env(safe-area-inset-bottom)' }}
      aria-label="Primary"
    >
      <ul className="mx-auto flex max-w-lg items-stretch justify-around">
        {items.map((item) => (
          <li key={item.to} className="flex-1">
            <NavLink
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  'flex flex-col items-center gap-1 py-2 text-[11px] font-medium transition-colors',
                  isActive
                    ? 'text-primary'
                    : 'text-muted-foreground hover:text-foreground',
                )
              }
            >
              {({ isActive }) => (
                <>
                  <item.icon
                    className={cn('h-5 w-5', isActive && 'fill-primary/10')}
                    strokeWidth={isActive ? 2.4 : 2}
                  />
                  <span>{item.label}</span>
                </>
              )}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
