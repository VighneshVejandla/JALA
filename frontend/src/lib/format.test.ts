import { describe, expect, it } from 'vitest';
import {
  formatCurrency,
  formatDate,
  formatDateTime,
  formatKg,
  formatNumber,
} from './format';

describe('format helpers', () => {
  it('formatKg', () => {
    expect(formatKg(null)).toBe('—');
    expect(formatKg(undefined)).toBe('—');
    expect(formatKg(120)).toContain('120');
    expect(formatKg(120)).toContain('kg');
  });

  it('formatCurrency', () => {
    expect(formatCurrency(null)).toBe('—');
    expect(formatCurrency(45000)).toMatch(/45,000|45000/);
  });

  it('formatNumber', () => {
    expect(formatNumber(null)).toBe('—');
    expect(formatNumber(1000)).toMatch(/1,000|1000/);
  });

  it('formatDate', () => {
    expect(formatDate(null)).toBe('—');
    expect(formatDate('not-a-date')).toBe('—');
    expect(formatDate('2026-05-01')).toContain('2026');
  });

  it('formatDateTime', () => {
    expect(formatDateTime(null)).toBe('—');
    expect(formatDateTime('bad')).toBe('—');
    expect(formatDateTime('2026-05-01T10:30:00')).toBeTruthy();
  });
});
