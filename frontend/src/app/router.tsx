import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { ROUTES } from '@/constants/routes';
import { AppShell } from '@/components/layout/AppShell';
import { LoginPage } from '@/features/auth/LoginPage';
import { RootRedirect } from '@/app/RootRedirect';
import { NotFound } from '@/features/misc/NotFound';
import { Forbidden } from '@/features/misc/Forbidden';

// User experience
import { UserHome } from '@/features/user/UserHome';
import { PondsPage } from '@/features/user/PondsPage';
import { PondDetailPage } from '@/features/user/PondDetailPage';
// Driver experience
import { DriverDeliveries } from '@/features/driver/DriverDeliveries';
import { DriverDeliveryDetail } from '@/features/driver/DriverDeliveryDetail';
// Admin experience
import { AdminDashboard } from '@/features/admin/AdminDashboard';
import { SitesPage } from '@/features/admin/SitesPage';
import { SiteDetail } from '@/features/admin/SiteDetail';
import { PondManage } from '@/features/admin/pond/PondManage';
import { UsersPage } from '@/features/admin/UsersPage';
import { HistoryPage } from '@/features/admin/HistoryPage';
import { HarvestedPage } from '@/features/admin/HarvestedPage';
import { InventoryPage } from '@/features/admin/InventoryPage';
import { AnalyticsPage } from '@/features/admin/AnalyticsPage';
// Shared
import { AlertsPage } from '@/features/shared/AlertsPage';
import { ProfilePage } from '@/features/shared/ProfilePage';

export function AppRoutes() {
  return (
    <Routes>
      <Route path={ROUTES.login} element={<LoginPage />} />

      {/* User (WORKER / MANAGER / SUPERVISOR) */}
      <Route element={<ProtectedRoute allow={['user']} />}>
        <Route path={ROUTES.app} element={<AppShell />}>
          <Route index element={<UserHome />} />
          <Route path="ponds" element={<PondsPage />} />
          <Route path="ponds/:pondId" element={<PondDetailPage />} />
          <Route path="alerts" element={<AlertsPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Driver */}
      <Route element={<ProtectedRoute allow={['driver']} />}>
        <Route path={ROUTES.driver} element={<AppShell />}>
          <Route index element={<DriverDeliveries />} />
          <Route path="deliveries/:id" element={<DriverDeliveryDetail />} />
          <Route path="alerts" element={<AlertsPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Admin */}
      <Route element={<ProtectedRoute allow={['admin']} />}>
        <Route path={ROUTES.admin} element={<AppShell />}>
          <Route index element={<AdminDashboard />} />
          <Route path="sites" element={<SitesPage />} />
          <Route path="sites/:siteId" element={<SiteDetail />} />
          <Route path="ponds/:pondId" element={<PondManage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="harvested" element={<HarvestedPage />} />
          <Route path="history" element={<HistoryPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="analytics" element={<AnalyticsPage />} />
          <Route path="alerts" element={<AlertsPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Root: send each role to its own home once authenticated */}
      <Route path={ROUTES.root} element={<RootRedirect />} />
      <Route path="/403" element={<Forbidden />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
