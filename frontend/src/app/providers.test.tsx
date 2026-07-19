import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AppProviders, shouldRetry } from './providers';
import { ApiError } from '@/api/client';

describe('shouldRetry', () => {
  it('never retries auth/permission/not-found errors', () => {
    expect(shouldRetry(0, new ApiError('no', 401))).toBe(false);
    expect(shouldRetry(0, new ApiError('no', 403))).toBe(false);
    expect(shouldRetry(0, new ApiError('no', 404))).toBe(false);
  });

  it('retries transient errors up to twice', () => {
    expect(shouldRetry(0, new ApiError('boom', 500))).toBe(true);
    expect(shouldRetry(1, new Error('net'))).toBe(true);
    expect(shouldRetry(2, new Error('net'))).toBe(false);
  });
});

describe('AppProviders', () => {
  it('renders its children', () => {
    render(
      <AppProviders>
        <p>inside</p>
      </AppProviders>,
    );
    expect(screen.getByText('inside')).toBeInTheDocument();
  });
});
