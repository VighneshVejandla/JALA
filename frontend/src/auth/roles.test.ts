import { describe, expect, it } from 'vitest';
import { experienceOf, homePathFor, ROLE_LABELS } from './roles';
import type { BackendRole } from '@/api/types';

describe('experienceOf', () => {
  it('maps ADMIN to admin', () => {
    expect(experienceOf('ADMIN')).toBe('admin');
  });

  it('maps DRIVER to driver', () => {
    expect(experienceOf('DRIVER')).toBe('driver');
  });

  it.each<BackendRole>(['MANAGER', 'SUPERVISOR', 'WORKER'])(
    'folds %s into the user experience',
    (role) => {
      expect(experienceOf(role)).toBe('user');
    },
  );
});

describe('homePathFor', () => {
  it('routes each experience to its home', () => {
    expect(homePathFor('admin')).toBe('/admin');
    expect(homePathFor('driver')).toBe('/driver');
    expect(homePathFor('user')).toBe('/app');
  });
});

describe('ROLE_LABELS', () => {
  it('has a label for every backend role', () => {
    (['ADMIN', 'MANAGER', 'SUPERVISOR', 'WORKER', 'DRIVER'] as BackendRole[]).forEach(
      (r) => expect(ROLE_LABELS[r]).toBeTruthy(),
    );
  });
});
