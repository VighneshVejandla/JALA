import type { BackendRole } from '@/api/types';
import { ROUTES } from '@/constants/routes';

/**
 * The three product experiences. Backend has five roles; we fold them:
 *   ADMIN                       -> admin
 *   MANAGER, SUPERVISOR, WORKER -> user   (site operations & data entry)
 *   DRIVER                      -> driver (feed delivery)
 */
export type Experience = 'admin' | 'user' | 'driver';

export function experienceOf(role: BackendRole): Experience {
  switch (role) {
    case 'ADMIN':
      return 'admin';
    case 'DRIVER':
      return 'driver';
    case 'MANAGER':
    case 'SUPERVISOR':
    case 'WORKER':
    default:
      return 'user';
  }
}

/** Landing route for each experience after login. */
export function homePathFor(exp: Experience): string {
  switch (exp) {
    case 'admin':
      return ROUTES.admin;
    case 'driver':
      return ROUTES.driver;
    case 'user':
    default:
      return ROUTES.app;
  }
}

export const ROLE_LABELS: Record<BackendRole, string> = {
  ADMIN: 'Administrator',
  MANAGER: 'Manager',
  SUPERVISOR: 'Supervisor',
  WORKER: 'Field Worker',
  DRIVER: 'Delivery Driver',
};
