import {
	env,
	createExecutionContext,
	waitOnExecutionContext,
} from "cloudflare:test";
import { describe, it, expect, vi } from "vitest";
import worker from "../src/index";

const IncomingRequest = Request<unknown, IncomingRequestCfProperties>;

describe("Telegram Subscription Worker - End to End", () => {
	const mockSubs = [
		{
			id: 1,
			uuid: "123e4567-e89b-12d3-a456-426614174000",
			payment_due_date: "2026-08-23",
			status: "active",
			price_sold: 35.0,
			clients: { id: 1, name: "Carlos Perez", email: "carlos@example.com", phone: "12345678" },
			services: { id: 1, name: "Netflix UHD" },
			profiles: { id: 1, name: "Perfil 1" },
		},
	];

	const mockAccounts = [
		{
			id: 2,
			uuid: "223e4567-e89b-12d3-a456-426614174001",
			email: "master@disney.com",
			source: "DigitalStore",
			renewal_date: "2026-08-23",
			status: "active",
			services: { id: 2, name: "Disney+" },
		},
	];

	it("processes GET request and sends consolidated telegram report with inline buttons", async () => {
		let telegramSentBody: any = null;

		const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input: any, init: any) => {
			const url = typeof input === "string" ? input : input.url;
			if (url.includes("/subscriptions")) {
				return new Response(JSON.stringify(mockSubs), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			if (url.includes("/accounts")) {
				return new Response(JSON.stringify(mockAccounts), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			if (url.includes("api.telegram.org/bot") && url.includes("/sendMessage")) {
				telegramSentBody = JSON.parse(init.body);
				return new Response(JSON.stringify({ ok: true, result: { message_id: 10 } }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			return new Response("Not Found", { status: 404 });
		});

		const request = new IncomingRequest("http://example.com?date=2026-08-23");
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, env, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(200);
		const json = await response.json();
		expect(json.ok).toBe(true);
		expect(json.total).toBe(2);
		expect(json.totalSubscriptions).toBe(1);
		expect(json.totalAccounts).toBe(1);

		expect(telegramSentBody).not.toBeNull();
		expect(telegramSentBody.text).toContain("Carlos Perez");
		expect(telegramSentBody.text).toContain("master@disney.com");
		expect(telegramSentBody.text).toContain("DigitalStore");
		expect(telegramSentBody.reply_markup.inline_keyboard[0][0].callback_data).toBe("send:1:0");

		fetchSpy.mockRestore();
	});

	it("processes Telegram Webhook POST callback_query to send email on button click", async () => {
		let resendPayload: any = null;
		let answeredCallbackText: string = "";
		let editedMarkup: any = null;

		const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input: any, init: any) => {
			const url = typeof input === "string" ? input : input.url;

			// Supabase single subscription query or insert
			if (url.includes("supabase.co") && url.includes("/subscriptions")) {
				return new Response(JSON.stringify(mockSubs[0]), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			if (url.includes("supabase.co") && url.includes("/notification_log")) {
				return new Response(JSON.stringify({ id: 1 }), {
					status: 201,
					headers: { "Content-Type": "application/json" },
				});
			}

			// Resend API
			if (url.includes("api.resend.com/emails")) {
				resendPayload = JSON.parse(init.body);
				return new Response(JSON.stringify({ id: "resend_123" }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}

			// Telegram answers and edits
			if (url.includes("/answerCallbackQuery")) {
				const body = JSON.parse(init.body);
				answeredCallbackText = body.text;
				return new Response(JSON.stringify({ ok: true }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}

			if (url.includes("/editMessageReplyMarkup")) {
				const body = JSON.parse(init.body);
				editedMarkup = body.reply_markup;
				return new Response(JSON.stringify({ ok: true }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}

			return new Response("Not Found", { status: 404 });
		});

		const envWithResend = {
			...env,
			RESEND_API_KEY: "re_mock_key",
			TELEGRAM_CHAT_ID: "123456789",
		};

		const webhookPayload = {
			update_id: 1001,
			callback_query: {
				id: "cb_query_999",
				from: { id: "123456789", first_name: "Alex" },
				message: {
					message_id: 55,
					chat: { id: "123456789" },
					reply_markup: {
						inline_keyboard: [
							[{ text: "📧 Enviar a Carlos (Hoy)", callback_data: "send:1:0" }],
						],
					},
				},
				data: "send:1:0",
			},
		};

		const request = new IncomingRequest("http://example.com/webhook", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(webhookPayload),
		});

		const ctx = createExecutionContext();
		const response = await worker.fetch(request, envWithResend, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(200);
		const json = await response.json();
		expect(json.ok).toBe(true);
		expect(json.emailSent).toBe(true);

		// Assertions on Resend and Telegram interactions
		expect(resendPayload).not.toBeNull();
		expect(resendPayload.to).toEqual(["carlos@example.com"]);
		expect(answeredCallbackText).toContain("carlos@example.com");
		expect(editedMarkup.inline_keyboard[0][0].text).toContain("Correo Enviado");
		expect(editedMarkup.inline_keyboard[0][0].callback_data).toBe("noop");

		fetchSpy.mockRestore();
	});

	it("rejects unauthorized HTTP GET when CRON_SECRET is set", async () => {
		const envWithSecret = { ...env, CRON_SECRET: "supersecret123" };
		const request = new IncomingRequest("http://example.com?date=2026-08-23");
		const ctx = createExecutionContext();
		const response = await worker.fetch(request, envWithSecret, ctx);
		await waitOnExecutionContext(ctx);

		expect(response.status).toBe(401);
		const json = await response.json();
		expect(json).toEqual({ ok: false, error: "Unauthorized" });
	});

	it("executes scheduled cron handler without errors", async () => {
		const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input: any) => {
			const url = typeof input === "string" ? input : input.url;
			if (url.includes("supabase.co") || url.includes("/rest/v1/")) {
				return new Response(JSON.stringify([]), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			if (url.includes("api.telegram.org")) {
				return new Response(JSON.stringify({ ok: true, result: { message_id: 13 } }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			return new Response("Not Found", { status: 404 });
		});

		const ctx = createExecutionContext();
		const controller = {
			cron: "0 14 * * *",
			scheduledTime: Date.now(),
			noRetry() {},
		} as ScheduledController;

		await worker.scheduled(controller, env, ctx);
		await waitOnExecutionContext(ctx);

		expect(fetchSpy).toHaveBeenCalled();
		fetchSpy.mockRestore();
	});
});
