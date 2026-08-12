import { verifyJwt } from "./jwt";
import type { Env } from "./types";

/** Headers that only the gateway is allowed to set. */
const TRUSTED_HEADERS = ["X-User-Id", "X-User-Role", "X-Gateway-Secret"] as const;

/** Paths that skip JWT verification entirely. */
const PUBLIC_PATHS: readonly string[] = [
	"/api/v1/auth/clients",
];

/**
 * Returns `true` when the request path matches any entry in {@link PUBLIC_PATHS}.
 *
 * Comparison is case-insensitive and ignores trailing slashes so that
 * `/api/v1/auth/clients/` is treated the same as `/api/v1/auth/clients`.
 */
function isPublicPath(pathname: string): boolean {
	const normalized = pathname.replace(/\/+$/, "").toLowerCase();
	return PUBLIC_PATHS.some((p) => normalized === p.toLowerCase());
}

export default {
	async fetch(request: Request, env: Env): Promise<Response> {
		const url = new URL(request.url);

		/* ── CORS preflight ───────────────────────────────────────── */
		if (request.method === "OPTIONS") {
			return new Response(null, {
				status: 204,
				headers: {
					"Access-Control-Allow-Origin": "*",
					"Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
					"Access-Control-Allow-Headers": "*",
					"Access-Control-Max-Age": "86400",
				},
			});
		}

		/* ── Health probe ─────────────────────────────────────────── */
		if (url.pathname === "/__gateway/health") {
			return new Response("ok", { status: 200 });
		}

		/* ── Strip trusted headers from the incoming request ────── */
		const upstreamHeaders = new Headers(request.headers);
		for (const h of TRUSTED_HEADERS) {
			upstreamHeaders.delete(h);
		}

		/* ── Public paths: forward without JWT verification ─────── */
		if (isPublicPath(url.pathname)) {
			return proxyToUpstream(request, url, upstreamHeaders, env);
		}

		/* ── Extract Bearer token ─────────────────────────────────── */
		const authHeader = request.headers.get("Authorization");
		if (!authHeader?.startsWith("Bearer ")) {
			return Response.json(
				{ error: "Missing or malformed Authorization header" },
				{ status: 401 },
			);
		}
		const token = authHeader.slice(7);

		/* ── Verify JWT ───────────────────────────────────────────── */
		const result = await verifyJwt(token, env);
		if (!result.ok) {
			return Response.json({ error: result.error }, { status: 401 });
		}

		/* ── Inject identity headers ──────────────────────────────── */
		upstreamHeaders.set("X-User-Id", result.payload.sub);
		upstreamHeaders.set("X-User-Role", result.payload.role);
		upstreamHeaders.set("X-Gateway-Secret", env.GATEWAY_SECRET);

		/* ── Proxy to upstream ────────────────────────────────────── */
		return proxyToUpstream(request, url, upstreamHeaders, env);
	},
} satisfies ExportedHandler<Env>;

/**
 * Forwards the request to the upstream Spring Boot API, attaching
 * CORS headers to the response.
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
			}),
		);

		const responseHeaders = new Headers(upstreamResponse.headers);
		responseHeaders.set("Access-Control-Allow-Origin", "*");

		return new Response(upstreamResponse.body, {
			status: upstreamResponse.status,
			statusText: upstreamResponse.statusText,
			headers: responseHeaders,
		});
	} catch (err: unknown) {
		const message = err instanceof Error ? err.message : "Unknown error";
		return Response.json(
			{ error: "Bad Gateway", message: `Failed to connect to upstream: ${message}` },
			{ status: 502 },
		);
	}
}
