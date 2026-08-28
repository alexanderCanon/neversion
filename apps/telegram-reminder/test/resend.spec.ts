import { env } from "cloudflare:test";
import { describe, it, expect, vi } from "vitest";
import { sendRenewalEmail } from "../src/services/resend.service";
import { FormattedSubscription } from "../src/types/subscription";

describe("Resend Email Service", () => {
	const mockSubscription: FormattedSubscription = {
		id: 101,
		uuid: "sub-uuid-101",
		clientName: "Alex Canon",
		clientEmail: "alex@example.com",
		serviceName: "Spotify Family",
		profileName: "Perfil 1",
		paymentDueDate: "2026-08-30",
		price: "45.00",
		daysRemaining: 3,
	};

	it("fails gracefully when RESEND_API_KEY is not configured", async () => {
		const envWithoutKey = { ...env, RESEND_API_KEY: undefined };
		const result = await sendRenewalEmail(envWithoutKey, mockSubscription);

		expect(result.ok).toBe(false);
		expect(result.error).toContain("RESEND_API_KEY no está configurada");
	});

	it("fails gracefully when client has no email", async () => {
		const envWithKey = { ...env, RESEND_API_KEY: "re_test_123" };
		const subWithoutEmail = { ...mockSubscription, clientEmail: undefined };
		const result = await sendRenewalEmail(envWithKey, subWithoutEmail);

		expect(result.ok).toBe(false);
		expect(result.error).toContain("no tiene correo electrónico");
	});

	it("successfully dispatches email when parameters are valid", async () => {
		const envWithKey = { ...env, RESEND_API_KEY: "re_test_123" };

		const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input: any, init: any) => {
			const url = typeof input === "string" ? input : input.url;
			if (url.includes("api.resend.com/emails")) {
				const body = JSON.parse(init.body);
				expect(body.to).toEqual(["alex@example.com"]);
				expect(body.subject).toContain("3 días");
				expect(body.html).toContain("Spotify Family");
				expect(body.text).toContain("Spotify Family");
				expect(body.reply_to).toBeDefined();
				return new Response(JSON.stringify({ id: "resend_msg_123" }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			return new Response("Not Found", { status: 404 });
		});

		const result = await sendRenewalEmail(envWithKey, mockSubscription);

		expect(result.ok).toBe(true);
		expect(result.id).toBe("resend_msg_123");

		fetchSpy.mockRestore();
	});
});
