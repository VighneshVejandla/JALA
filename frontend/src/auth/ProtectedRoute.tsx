import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { homePathFor, type Experience } from './roles';
import { FullScreenLoader } from '@/components/common/FullScreenLoader';

/**
 * Guards a route subtree. Redirects unauthenticated users to /login and
 * users whose experience is not in `allow` to their own home.
 */
export function ProtectedRoute({ allow }: { allow?: Experience[] }) {
  const { isAuthenticated, isLoading, experience } = useAuth();
  const location = useLocation();

  if (isLoading) return <FullScreenLoader />;

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (allow && experience && !allow.includes(experience)) {
    return <Navigate to={homePathFor(experience)} replace />;
  }

  return <Outlet />;
}
