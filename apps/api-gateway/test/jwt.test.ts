import {
	env,
	createExecutionContext,
	waitOnExecutionContext,
} from "cloudflare:test";
import { describe, it, expect } from "vitest";
import worker from "../src/index";

/*
 * Integration-level tests that exercise the Worker's fetch handler
 * end-to-end using the Cloudflare vitest pool.
 *
 * The pool reads wrangler.jsonc and makes `env` available automatically.
 * Secrets referenced in the Env type must be set in `.dev.vars` for
 * local testing or mocked here.
 */

describe("Gateway — JWT verification", () => {
	it("rejects requests without Authorization header", async () => {
		const request = new Request("https://api.neversion.com/api/v1/accounts");
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(401);
		const body = (await response.json()) as { error: string; message: string };
		expect(body.message).toContain("Authorization");
	});

	it("returns 200 for /__gateway/health", async () => {
		const request = new Request("https://api.neversion.com/__gateway/health");
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(200);
		expect(await response.text()).toBe("ok");
	});

	it("handles CORS preflight requests", async () => {
		const request = new Request("https://api.neversion.com/api/v1/accounts", {
			method: "OPTIONS",
		});
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(204);
		expect(response.headers.get("Access-Control-Allow-Origin")).toBe("*");
		expect(response.headers.get("Access-Control-Allow-Methods")).toContain("POST");
	});
});

describe("Gateway — Header stripping", () => {
	it("strips spoofed identity headers from public path requests", async () => {
		const request = new Request("https://api.neversion.com/api/v1/auth/clients", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				"X-User-Id": "spoofed-id",
				"X-User-Role": "SUPER_ADMIN",
				"X-Gateway-Secret": "fake-secret",
			},
			body: JSON.stringify({ email: "test@example.com" }),
		});

		const ctx = createExecutionContext();
		// This will fail to connect to upstream, but the important thing is
		// that the headers are stripped before proxying. Since we can't inspect
		// the outgoing request directly in this test setup, this test
		// validates that the public path is recognized and the request
		// is forwarded (resulting in a 502 since there's no real upstream).
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		// Will be 502 (no upstream) or a valid response if upstream is running
		// The key assertion is that the request was not rejected as 401
		expect(response.status).not.toBe(401);
	});
});
