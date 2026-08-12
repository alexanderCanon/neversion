/**
 * Cloudflare Worker binding types.
 *
 * Secrets are set via `wrangler secret put <NAME>` and are never
 * checked into version control.
 */
export interface Env {
	JWT_HMAC_SECRET: string;
	GATEWAY_SECRET: string;
	UPSTREAM_URL: string;
	JWT_ISSUER: string;
	JWT_AUDIENCE: string;
}
