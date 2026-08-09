/**
 * Cloudflare Worker binding types.
 *
 * Secrets are set via `wrangler secret put <NAME>` and are never
 * checked into version control.
 */
export interface Env {
	/** HS256 shared secret used by Supabase GoTrue to sign JWTs. */
	JWT_HMAC_SECRET: string;

	/**
	 * Static secret attached as `X-Gateway-Secret` on every proxied
	 * request.  Must match the value expected by the Spring Boot backend.
	 * MUST be different from JWT_HMAC_SECRET.
	 */
	GATEWAY_SECRET: string;

	/**
	 * Base URL of the upstream Spring Boot API
	 * (e.g. "https://internal-api.neversion.com").
	 */
	UPSTREAM_URL: string;

	/**
	 * Expected `iss` (issuer) claim inside the JWT.
	 * Typically the public Supabase project URL.
	 */
	JWT_ISSUER: string;

	/**
	 * Expected `aud` (audience) claim inside the JWT.
	 * Supabase GoTrue defaults to "authenticated".
	 */
	JWT_AUDIENCE: string;
}
