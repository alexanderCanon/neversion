/**
 * Security headers and standard response utilities for the API Gateway.
 */

/** Headers that only the gateway is allowed to set on upstream requests. */
export const TRUSTED_HEADERS = [
	"X-User-Id",
	"X-User-Role",
	"X-Gateway-Secret",
] as const;

/** Standard security headers attached to every response leaving the gateway. */
export const DEFAULT_SECURITY_HEADERS: Record<string, string> = {
	"X-Content-Type-Options": "nosniff",
	"X-Frame-Options": "DENY",
	"Referrer-Policy": "strict-origin-when-cross-origin",
	"X-XSS-Protection": "1; mode=block",
	"Access-Control-Allow-Origin": "*",
};

/** CORS preflight response headers. */
export const CORS_PREFLIGHT_HEADERS: Record<string, string> = {
	...DEFAULT_SECURITY_HEADERS,
	"Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
	"Access-Control-Allow-Headers": "Authorization, Content-Type, Accept, Origin, X-Requested-With",
	"Access-Control-Max-Age": "86400",
};

/**
 * Creates a standard JSON error response with full CORS and security headers.
 */
export function createErrorResponse(
	error: string,
	message: string,
	status: number,
	extraHeaders?: Record<string, string>,
): Response {
	const headers = new Headers({
		"Content-Type": "application/json; charset=utf-8",
		...DEFAULT_SECURITY_HEADERS,
		...(extraHeaders ?? {}),
	});

	const body = JSON.stringify({
		error,
		message,
		status,
		timestamp: new Date().toISOString(),
	});

	return new Response(body, { status, headers });
}

/**
 * Injects security headers into an outgoing upstream response.
 */
export function applySecurityHeaders(headers: Headers): Headers {
	for (const [key, value] of Object.entries(DEFAULT_SECURITY_HEADERS)) {
		if (!headers.has(key)) {
			headers.set(key, value);
		}
	}
	return headers;
}
