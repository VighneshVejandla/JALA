import {
  Bell,
  Home,
  LayoutDashboard,
  MapPin,
  Truck,
  User,
  Users,
  Waves,
  type LucideIcon,
} from 'lucide-react';
import type { Experience } from '@/auth/roles';

export interface NavItem {
  to: string;
  label: string;
  icon: LucideIcon;
  end?: boolean;
}

export const NAV_BY_EXPERIENCE: Record<Experience, NavItem[]> = {
  user: [
    { to: '/app', label: 'Home', icon: Home, end: true },
    { to: '/app/ponds', label: 'Ponds', icon: Waves },
    { to: '/app/alerts', label: 'Alerts', icon: Bell },
    { to: '/app/profile', label: 'Profile', icon: User },
  ],
  driver: [
    { to: '/driver', label: 'Deliveries', icon: Truck, end: true },
    { to: '/driver/profile', label: 'Profile', icon: User },
  ],
  admin: [
    { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/admin/sites', label: 'Sites', icon: MapPin },
    { to: '/admin/users', label: 'Users', icon: Users },
    { to: '/admin/alerts', label: 'Alerts', icon: Bell },
    { to: '/admin/profile', label: 'Profile', icon: User },
  ],
};
