import { verifyJwt } from "./jwt";
import { isAuthMutationPath, isPublicPath } from "./public-paths";
import { rateLimiter, RATE_LIMIT_PROFILES } from "./rate-limiter";
import {
	applySecurityHeaders,
	CORS_PREFLIGHT_HEADERS,
	createErrorResponse,
	TRUSTED_HEADERS,
} from "./security";
import type { Env } from "./types";

export default {
	async fetch(request: Request, env: Env): Promise<Response> {
		const url = new URL(request.url);

		/* ── 1. CORS Preflight ─────────────────────────────────────── */
		if (request.method === "OPTIONS") {
			return new Response(null, {
				status: 204,
				headers: CORS_PREFLIGHT_HEADERS,
			});
		}

		/* ── 2. Health Probe ───────────────────────────────────────── */
		if (url.pathname === "/__gateway/health") {
			return new Response("ok", {
				status: 200,
				headers: { "Content-Type": "text/plain; charset=utf-8" },
			});
		}

		/* ── 3. Edge Rate Limiting ─────────────────────────────────── */
		const clientIp =
			request.headers.get("CF-Connecting-IP") ||
			request.headers.get("X-Forwarded-For") ||
			"global";

		if (isAuthMutationPath(url.pathname, request.method)) {
			const limitKey = `auth:${clientIp}`;
			const rateCheck = rateLimiter.check(limitKey, RATE_LIMIT_PROFILES.AUTH_MUTATION);
			if (!rateCheck.allowed) {
				return createErrorResponse(
					"Too Many Requests",
					`Too many registration attempts. Please try again in ${rateCheck.resetSeconds} seconds.`,
					429,
					{
						"Retry-After": String(rateCheck.resetSeconds),
						"RateLimit-Limit": String(rateCheck.limit),
						"RateLimit-Remaining": "0",
						"RateLimit-Reset": String(rateCheck.resetSeconds),
					},
				);
			}
		}

		/* ── 4. Strip spoofed trusted headers from incoming request ─ */
		const upstreamHeaders = new Headers(request.headers);
		for (const h of TRUSTED_HEADERS) {
			upstreamHeaders.delete(h);
		}

		/* ── 5. Public Paths: forward without JWT verification ───── */
		if (isPublicPath(url.pathname)) {
			return proxyToUpstream(request, url, upstreamHeaders, env);
		}

		/* ── 6. Extract Bearer Token ──────────────────────────────── */
		const authHeader = request.headers.get("Authorization");
		if (!authHeader?.startsWith("Bearer ")) {
			return createErrorResponse(
				"Unauthorized",
				"Missing or malformed Authorization header",
				401,
			);
		}
		const token = authHeader.slice(7);

		/* ── 7. Verify JWT ─────────────────────────────────────────── */
		const result = await verifyJwt(token, env);
		if (!result.ok) {
			return createErrorResponse("Unauthorized", result.error, 401);
		}

		/* ── 8. Inject Trusted Identity Headers ───────────────────── */
		upstreamHeaders.set("X-User-Id", result.payload.sub);
		upstreamHeaders.set("X-User-Role", result.payload.role);
		upstreamHeaders.set("X-Gateway-Secret", env.GATEWAY_SECRET);

		/* ── 9. Proxy to Upstream ──────────────────────────────────── */
		return proxyToUpstream(request, url, upstreamHeaders, env);
	},
} satisfies ExportedHandler<Env>;

/**
 * Forwards the request to the upstream Spring Boot API with timeout and security headers.
 */
async function proxyToUpstream(
	request: Request,
	url: URL,
	headers: Headers,
	env: Env,
): Promise<Response> {
	const upstream = new URL(url.pathname + url.search, env.UPSTREAM_URL);

	try {
		const upstreamResponse = await fetch(
			new Request(upstream.toString(), {
				method: request.method,
				headers,
				body: ["GET", "HEAD"].includes(request.method) ? null : request.body,
				signal: AbortSignal.timeout(15_000), // 15-second upstream timeout
			}),
		);

		const responseHeaders = applySecurityHeaders(new Headers(upstreamResponse.headers));

		return new Response(upstreamResponse.body, {
			status: upstreamResponse.status,
			statusText: upstreamResponse.statusText,
			headers: responseHeaders,
		});
	} catch (err: unknown) {
		if (err instanceof Error && err.name === "TimeoutError") {
			return createErrorResponse(
				"Gateway Timeout",
				"The upstream service timed out while processing your request.",
				504,
			);
		}

		const message = err instanceof Error ? err.message : "Unknown error";
		return createErrorResponse(
			"Bad Gateway",
			`Failed to connect to upstream service: ${message}`,
			502,
		);
	}
}
