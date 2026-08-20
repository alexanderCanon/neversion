import {
	env,
	createExecutionContext,
	waitOnExecutionContext,
} from "cloudflare:test";
import { describe, it, expect } from "vitest";
import worker from "../src/index";

describe("Gateway — Security & Rate Limiting", () => {
	it("attaches security headers to responses", async () => {
		const request = new Request("https://api.neversion.com/api/v1/accounts");
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.headers.get("X-Content-Type-Options")).toBe("nosniff");
		expect(response.headers.get("X-Frame-Options")).toBe("DENY");
		expect(response.headers.get("Access-Control-Allow-Origin")).toBe("*");
	});

	it("rate limits excessive public auth registration attempts", async () => {
		const ctx = createExecutionContext();
		const makeRequest = () =>
			worker.fetch(
				new Request("https://api.neversion.com/api/v1/auth/clients", {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						"CF-Connecting-IP": "203.0.113.195",
					},
					body: JSON.stringify({ email: "spammer@example.com" }),
				}),
				env,
				ctx,
			);

		// Send 10 allowed requests
		for (let i = 0; i < 10; i++) {
			await makeRequest();
		}

		// The 11th request must be rejected with 429 Too Many Requests
		const response = await makeRequest();
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(429);
		const body = (await response.json()) as { error: string; message: string };
		expect(body.error).toBe("Too Many Requests");
		expect(response.headers.get("Retry-After")).toBeDefined();
	});

	it("recognizes public vendor paths without JWT", async () => {
		const request = new Request("https://api.neversion.com/api/v1/vendors/public/store-123");
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		// Must not be rejected as 401 Unauthorized
		expect(response.status).not.toBe(401);
	});
});
