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
import { ROUTES } from '@/constants/routes';

export interface NavItem {
  to: string;
  label: string;
  icon: LucideIcon;
  end?: boolean;
}

export const NAV_BY_EXPERIENCE: Record<Experience, NavItem[]> = {
  user: [
    { to: ROUTES.app, label: 'Home', icon: Home, end: true },
    { to: ROUTES.ponds, label: 'Ponds', icon: Waves },
    { to: ROUTES.appAlerts, label: 'Alerts', icon: Bell },
    { to: ROUTES.appProfile, label: 'Profile', icon: User },
  ],
  driver: [
    { to: ROUTES.driver, label: 'Deliveries', icon: Truck, end: true },
    { to: ROUTES.driverProfile, label: 'Profile', icon: User },
  ],
  admin: [
    { to: ROUTES.admin, label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: ROUTES.adminSites, label: 'Sites', icon: MapPin },
    { to: ROUTES.adminUsers, label: 'Users', icon: Users },
    { to: ROUTES.adminAlerts, label: 'Alerts', icon: Bell },
    { to: ROUTES.adminProfile, label: 'Profile', icon: User },
  ],
};
