import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { api } from '@/api/endpoints';
import { setUnauthorizedHandler, tokenStore } from '@/api/client';
import type { BackendRole, UserResponse } from '@/api/types';
import { experienceOf, type Experience } from './roles';

interface AuthState {
  user: UserResponse | null;
  role: BackendRole | null;
  experience: Experience | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (employeeCode: string, password: string) => Promise<UserResponse>;
  logout: () => void;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(!!tokenStore.get());

  const logout = useCallback(() => {
    tokenStore.clear();
    setUser(null);
  }, []);

  const refresh = useCallback(async () => {
    if (!tokenStore.get()) {
      setUser(null);
      setIsLoading(false);
      return;
    }
    try {
      const me = await api.auth.me();
      setUser(me);
    } catch {
      // Token invalid/expired — the client interceptor already cleared it.
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback(async (employeeCode: string, password: string) => {
    const res = await api.auth.login({ employeeCode, password });
    tokenStore.set(res.accessToken);
    const me = await api.auth.me();
    setUser(me);
    return me;
  }, []);

  // Wire the axios 401 handler to force logout on session expiry.
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
  }, []);

  // Restore session on first load.
  useEffect(() => {
    void refresh();
  }, [refresh]);

  const value = useMemo<AuthState>(
    () => ({
      user,
      role: user?.role ?? null,
      experience: user ? experienceOf(user.role) : null,
      isAuthenticated: !!user,
      isLoading,
      login,
      logout,
      refresh,
    }),
    [user, isLoading, login, logout, refresh],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
