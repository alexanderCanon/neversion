import type { Env } from "./types";

/** Minimal JWT payload fields used by the gateway. */
export interface JwtPayload {
	sub: string;
	role: string;
	exp: number;
	nbf?: number;
	iss?: string;
	aud?: string | string[];
	app_metadata?: { role?: string };
}

type VerifyResult =
	| { ok: true; payload: JwtPayload }
	| { ok: false; error: string };

/**
 * Verifies a Auth Provider HS256 JWT using Web Crypto HMAC-SHA256.
 *
 * Steps:
 * 1. Split the compact token into header / payload / signature.
 * 2. Re-compute the HMAC-SHA256 signature with the shared secret.
 * 3. Compare signatures in constant time.
 * 4. Validate standard claims (exp, nbf, iss, aud).
 */
export async function verifyJwt(
	token: string,
	env: Env,
): Promise<VerifyResult> {
	const parts = token.split(".");
	if (parts.length !== 3) {
		return { ok: false, error: "Malformed JWT: expected 3 parts" };
	}

	const [headerB64, payloadB64, signatureB64] = parts;

	/* ── Import shared secret as HMAC key ────────────────────── */
	const encoder = new TextEncoder();
	const key = await crypto.subtle.importKey(
		"raw",
		encoder.encode(env.JWT_HMAC_SECRET),
		{ name: "HMAC", hash: "SHA-256" },
		false,
		["sign"],
	);

	/* ── Re-compute the signature ────────────────────────────── */
	const data = encoder.encode(`${headerB64}.${payloadB64}`);
	const computed = new Uint8Array(await crypto.subtle.sign("HMAC", key, data));

	/* ── Decode the token's signature ────────────────────────── */
	const provided = base64UrlDecode(signatureB64);

	/* ── Constant-time comparison ────────────────────────────── */
	if (!timingSafeEqual(computed, provided)) {
		return { ok: false, error: "Invalid signature" };
	}

	/* ── Decode and validate payload ─────────────────────────── */
	const payload: JwtPayload = JSON.parse(
		new TextDecoder().decode(base64UrlDecode(payloadB64)),
	);

	const now = Math.floor(Date.now() / 1000);

	if (typeof payload.exp !== "number" || now >= payload.exp) {
		return { ok: false, error: "Token expired" };
	}

	if (typeof payload.nbf === "number" && now < payload.nbf) {
		return { ok: false, error: "Token not yet valid" };
	}

	if (env.JWT_ISSUER && payload.iss !== env.JWT_ISSUER) {
		return { ok: false, error: "Invalid issuer" };
	}

	if (env.JWT_AUDIENCE) {
		const audiences = Array.isArray(payload.aud)
			? payload.aud
			: [payload.aud];
		if (!audiences.includes(env.JWT_AUDIENCE)) {
			return { ok: false, error: "Invalid audience" };
		}
	}

	/* ── Resolve role: prefer app_metadata.role, fall back to top-level role ── */
	const role =
		payload.app_metadata?.role ?? payload.role ?? "authenticated";
	payload.role = role;

	return { ok: true, payload };
}

/* ── Helpers ──────────────────────────────────────────────────── */

/** Decodes a Base64-URL string into a Uint8Array. */
function base64UrlDecode(input: string): Uint8Array {
	const base64 = input.replace(/-/g, "+").replace(/_/g, "/");
	const pad = base64.length % 4 === 0 ? "" : "=".repeat(4 - (base64.length % 4));
	const binary = atob(base64 + pad);
	return Uint8Array.from(binary, (c) => c.charCodeAt(0));
}

/**
 * Constant-time comparison of two byte arrays.
 * Returns `true` only when both arrays have the same length and content.
 */
function timingSafeEqual(a: Uint8Array, b: Uint8Array): boolean {
	if (a.length !== b.length) return false;
	let result = 0;
	for (let i = 0; i < a.length; i++) {
		result |= a[i] ^ b[i];
	}
	return result === 0;
}
