/**
 * Edge Rate Limiter based on an in-memory Sliding Window counter.
 *
 * Tracks request timestamps per client IP within a rolling time window.
 */

interface RateLimitConfig {
	limit: number;
	windowSeconds: number;
}

export interface RateLimitResult {
	allowed: boolean;
	limit: number;
	remaining: number;
	resetSeconds: number;
}

class InMemoryRateLimiter {
	private readonly storage = new Map<string, number[]>();
	private lastCleanup = Date.now();

	/**
	 * Evaluates whether a request from the given identifier is allowed.
	 */
	check(key: string, config: RateLimitConfig): RateLimitResult {
		const now = Date.now();
		const windowMs = config.windowSeconds * 1000;
		const windowStart = now - windowMs;

		this.cleanupOldEntries(now);

		const timestamps = this.storage.get(key)?.filter((t) => t > windowStart) ?? [];

		if (timestamps.length >= config.limit) {
			const oldestInWindow = timestamps[0] ?? now;
			const resetSeconds = Math.ceil((oldestInWindow + windowMs - now) / 1000);

			return {
				allowed: false,
				limit: config.limit,
				remaining: 0,
				resetSeconds: Math.max(1, resetSeconds),
			};
		}

		timestamps.push(now);
		this.storage.set(key, timestamps);

		return {
			allowed: true,
			limit: config.limit,
			remaining: config.limit - timestamps.length,
			resetSeconds: config.windowSeconds,
		};
	}

	/**
	 * Periodically prunes expired timestamps to prevent memory leaks in the worker runtime.
	 */
	private cleanupOldEntries(now: number): void {
		// Run cleanup every 60 seconds
		if (now - this.lastCleanup < 60_000) {
			return;
		}
		this.lastCleanup = now;

		const cutoff = now - 300_000; // 5 minutes retention
		for (const [key, timestamps] of this.storage.entries()) {
			const active = timestamps.filter((t) => t > cutoff);
			if (active.length === 0) {
				this.storage.delete(key);
			} else {
				this.storage.set(key, active);
			}
		}
	}
}

export const rateLimiter = new InMemoryRateLimiter();

/**
 * Predefined rate limit profiles.
 */
export const RATE_LIMIT_PROFILES = {
	/** Strict limit for public registration and auth actions: 10 requests / min */
	AUTH_MUTATION: { limit: 10, windowSeconds: 60 },
	/** General limit for public queries: 60 requests / min */
	PUBLIC_READ: { limit: 60, windowSeconds: 60 },
} as const;
