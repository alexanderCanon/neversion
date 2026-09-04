import { env } from "cloudflare:test";
import { describe, it, expect, vi } from "vitest";
import {
	buildConsolidatedReport,
	sendTelegramMessage,
	answerCallbackQuery,
	editMessageReplyMarkup,
} from "../src/services/telegram.service";
import { RenewalWindowGroup } from "../src/types/subscription";

describe("Telegram Service", () => {
	it("formats empty report correctly", () => {
		const emptyGroups: RenewalWindowGroup[] = [
			{ days: 3, label: "🟡 Vencen en 3 días", subscriptions: [] },
			{ days: 1, label: "🟠 Vencen mañana", subscriptions: [] },
			{ days: 0, label: "🔴 Vencen hoy", subscriptions: [] },
		];

		const report = buildConsolidatedReport(emptyGroups);
		expect(report.totalSubscriptions).toBe(0);
		expect(report.text).toContain("No hay suscripciones que venzan hoy ni en los próximos 3 días");
		expect(report.replyMarkup).toBeUndefined();
	});

	it("formats multi-window report with interactive inline buttons", () => {
		const groups: RenewalWindowGroup[] = [
			{
				days: 3,
				label: "🟡 Vencen en 3 días",
				subscriptions: [
					{
						id: 101,
						uuid: "sub-101",
						clientName: "Alex Canon",
						clientEmail: "alex@example.com",
						serviceName: "Netflix UHD",
						paymentDueDate: "2026-08-30",
						price: "35.00",
						daysRemaining: 3,
					},
				],
			},
			{
				days: 0,
				label: "🔴 Vencen hoy",
				subscriptions: [
					{
						id: 102,
						uuid: "sub-102",
						clientName: "Maria Lopez",
						serviceName: "Spotify",
						paymentDueDate: "2026-08-27",
						price: "25.00",
						daysRemaining: 0,
					},
				],
			},
		];

		const report = buildConsolidatedReport(groups);
		expect(report.totalSubscriptions).toBe(2);
		expect(report.text).toContain("Alex Canon");
		expect(report.text).toContain("Maria Lopez");
		expect(report.replyMarkup?.inline_keyboard).toHaveLength(2);
		expect(report.replyMarkup?.inline_keyboard[0][0].text).toContain("📧 Enviar a Alex Canon (3d)");
		expect(report.replyMarkup?.inline_keyboard[0][0].callback_data).toBe("send:101:3");
		expect(report.replyMarkup?.inline_keyboard[1][0].text).toContain("⚠️ Maria Lopez (Sin email)");
	});

	it("formats consolidated report with both client subscriptions and master accounts", () => {
		const subGroups: RenewalWindowGroup[] = [
			{
				days: 0,
				label: "🔴 Vencen hoy",
				subscriptions: [
					{
						id: 101,
						uuid: "sub-101",
						clientName: "Alex Canon",
						clientEmail: "alex@example.com",
						serviceName: "Netflix UHD",
						paymentDueDate: "2026-09-03",
						price: "35.00",
						daysRemaining: 0,
					},
				],
			},
		];

		const accountGroups = [
			{
				days: 1,
				label: "🟠 Vencen mañana (1 día)",
				accounts: [
					{
						id: 201,
						uuid: "acc-201",
						serviceName: "Disney+",
						email: "disney_master@example.com",
						source: "DigitalStore",
						renewalDate: "2026-09-04",
						daysRemaining: 1,
					},
				],
			},
		];

		const report = buildConsolidatedReport(subGroups, accountGroups);
		expect(report.totalSubscriptions).toBe(1);
		expect(report.totalAccounts).toBe(1);
		expect(report.text).toContain("🔔 *Reporte de Renovaciones de Clientes*");
		expect(report.text).toContain("🔑 *Cuentas Maestras por Renovar*");
		expect(report.text).toContain("Disney+");
		expect(report.text).toContain("disney_master@example.com");
		expect(report.text).toContain("DigitalStore");
		expect(report.text).toContain("2026-09-04");
		expect(report.text).toContain("1 suscripción(es) | 1 cuenta(s) maestra(s)");
		expect(report.replyMarkup?.inline_keyboard).toHaveLength(1);
	});

	it("formats report with only master accounts and no buttons", () => {
		const emptySubGroups: RenewalWindowGroup[] = [
			{ days: 3, label: "🟡 Vencen en 3 días", subscriptions: [] },
			{ days: 1, label: "🟠 Vencen mañana", subscriptions: [] },
			{ days: 0, label: "🔴 Vencen hoy", subscriptions: [] },
		];

		const accountGroups = [
			{
				days: 0,
				label: "🔴 Vencen hoy",
				accounts: [
					{
						id: 301,
						uuid: "acc-301",
						serviceName: "HBO Max",
						email: "hbo_master@example.com",
						source: "SupplierX",
						renewalDate: "2026-09-03",
						daysRemaining: 0,
					},
				],
			},
		];

		const report = buildConsolidatedReport(emptySubGroups, accountGroups);
		expect(report.totalSubscriptions).toBe(0);
		expect(report.totalAccounts).toBe(1);
		expect(report.text).not.toContain("🔔 *Reporte de Renovaciones de Clientes*");
		expect(report.text).toContain("🔑 *Cuentas Maestras por Renovar*");
		expect(report.text).toContain("HBO Max");
		expect(report.text).toContain("hbo_master@example.com");
		expect(report.text).toContain("SupplierX");
		expect(report.replyMarkup).toBeUndefined();
	});

	it("calls answerCallbackQuery correctly", async () => {
		const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input: any, init: any) => {
			const url = typeof input === "string" ? input : input.url;
			if (url.includes("answerCallbackQuery")) {
				const body = JSON.parse(init.body);
				expect(body.callback_query_id).toBe("query_123");
				expect(body.text).toBe("✅ Correo enviado");
				return new Response(JSON.stringify({ ok: true, result: true }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			return new Response("Not Found", { status: 404 });
		});

		const res = await answerCallbackQuery(env, "query_123", "✅ Correo enviado");
		expect(res.ok).toBe(true);

		fetchSpy.mockRestore();
	});

	it("calls editMessageReplyMarkup correctly", async () => {
		const fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async (input: any, init: any) => {
			const url = typeof input === "string" ? input : input.url;
			if (url.includes("editMessageReplyMarkup")) {
				const body = JSON.parse(init.body);
				expect(body.chat_id).toBe("123456789");
				expect(body.message_id).toBe(999);
				return new Response(JSON.stringify({ ok: true, result: true }), {
					status: 200,
					headers: { "Content-Type": "application/json" },
				});
			}
			return new Response("Not Found", { status: 404 });
		});

		const res = await editMessageReplyMarkup(env, "123456789", 999, {
			inline_keyboard: [[{ text: "✅ Enviado", callback_data: "noop" }]],
		});
		expect(res.ok).toBe(true);

		fetchSpy.mockRestore();
	});
});
