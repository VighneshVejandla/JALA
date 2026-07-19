import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { AppShell } from '@/components/layout/AppShell';
import { LoginPage } from '@/features/auth/LoginPage';
import { RootRedirect } from '@/app/RootRedirect';
import { NotFound } from '@/features/misc/NotFound';

// User experience
import { UserHome } from '@/features/user/UserHome';
import { PondsPage } from '@/features/user/PondsPage';
import { PondDetailPage } from '@/features/user/PondDetailPage';
// Driver experience
import { DriverDeliveries } from '@/features/driver/DriverDeliveries';
// Admin experience
import { AdminDashboard } from '@/features/admin/AdminDashboard';
import { SitesPage } from '@/features/admin/SitesPage';
import { UsersPage } from '@/features/admin/UsersPage';
// Shared
import { AlertsPage } from '@/features/shared/AlertsPage';
import { ProfilePage } from '@/features/shared/ProfilePage';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* User (WORKER / MANAGER / SUPERVISOR) */}
      <Route element={<ProtectedRoute allow={['user']} />}>
        <Route path="/app" element={<AppShell />}>
          <Route index element={<UserHome />} />
          <Route path="ponds" element={<PondsPage />} />
          <Route path="ponds/:pondId" element={<PondDetailPage />} />
          <Route path="alerts" element={<AlertsPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Driver */}
      <Route element={<ProtectedRoute allow={['driver']} />}>
        <Route path="/driver" element={<AppShell />}>
          <Route index element={<DriverDeliveries />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Admin */}
      <Route element={<ProtectedRoute allow={['admin']} />}>
        <Route path="/admin" element={<AppShell />}>
          <Route index element={<AdminDashboard />} />
          <Route path="sites" element={<SitesPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="alerts" element={<AlertsPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* Root: send each role to its own home once authenticated */}
      <Route path="/" element={<RootRedirect />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
