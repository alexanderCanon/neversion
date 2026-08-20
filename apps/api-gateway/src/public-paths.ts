/**
 * Paths that skip mandatory JWT verification.
 */

const PUBLIC_EXACT_PATHS: readonly string[] = [
	"/api/v1/auth/clients",
	"/v3/api-docs",
];

const PUBLIC_PREFIXES: readonly string[] = [
	"/api/v1/vendors/public",
	"/swagger-ui",
];

/**
 * Returns true if the pathname is public and does not require a JWT.
 */
export function isPublicPath(pathname: string): boolean {
	const normalized = pathname.replace(/\/+$/, "").toLowerCase();

	if (PUBLIC_EXACT_PATHS.some((p) => normalized === p.toLowerCase())) {
		return true;
	}

	return PUBLIC_PREFIXES.some((prefix) =>
		normalized.startsWith(prefix.toLowerCase()),
	);
}

/**
 * Returns true if the pathname represents a sensitive auth mutation endpoint.
 */
export function isAuthMutationPath(pathname: string, method: string): boolean {
	const normalized = pathname.replace(/\/+$/, "").toLowerCase();
	return normalized === "/api/v1/auth/clients" && method.toUpperCase() === "POST";
}
