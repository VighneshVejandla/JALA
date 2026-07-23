import { Navigate } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { homePathFor } from '@/auth/roles';
import { FullScreenLoader } from '@/components/common/FullScreenLoader';

/** Entry point at "/": route to the correct experience or to login. */
export function RootRedirect() {
  const { isLoading, isAuthenticated, experience } = useAuth();

  if (isLoading) return <FullScreenLoader />;
  if (!isAuthenticated || !experience) return <Navigate to="/login" replace />;
  return <Navigate to={homePathFor(experience)} replace />;
}
