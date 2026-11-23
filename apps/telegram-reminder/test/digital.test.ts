import { describe, expect, it } from 'vitest';
import { formatDigest } from '../src/digital';

describe('Neversion Digital', () => {
  it('distinguishes today, preserves names as text and shows omitted rows', () => {
    const digest = {
      today: '2026-11-23',
      total: 2,
      subscriptions: [{ client: 'Ana_*', service: 'Video', dueOn: '2026-11-23' }],
    };
    expect(formatDigest(digest)).toContain('Hoy · Ana_* · Video');
    expect(formatDigest(digest)).toContain('Otros vencimientos: 1');
  });
});
