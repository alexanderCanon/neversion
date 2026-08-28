import {
	getExpiringSubscriptionsByWindows,
	getSubscriptionById,
	logNotification,
} from "./services/supabase.service";
import {
	buildConsolidatedReport,
	sendTelegramMessage,
	answerCallbackQuery,
	editMessageReplyMarkup,
	InlineKeyboardMarkup,
} from "./services/telegram.service";
import { sendRenewalEmail } from "./services/resend.service";

/**
 * Orquesta la consulta de suscripciones por ventanas (3d, 1d, 0d) y el envío del reporte con botones a Telegram.
 */
async function runDailyReminder(env: Env, targetBaseDate?: string) {
	const groups = await getExpiringSubscriptionsByWindows(env, targetBaseDate);
	const { text, replyMarkup, totalSubscriptions } = buildConsolidatedReport(groups);

	await sendTelegramMessage(env, text, replyMarkup);

	return {
		total: totalSubscriptions,
		groups,
	};
}

/**
 * Procesa el evento callback_query recibido desde el Webhook de Telegram cuando el usuario presiona un botón.
 */
async function handleTelegramCallbackQuery(
	env: Env,
	callbackQuery: any
): Promise<Response> {
	const callbackId = callbackQuery.id;
	const data = callbackQuery.data as string;
	const fromId = String(callbackQuery.from?.id);
	const message = callbackQuery.message;
	const chatId = message?.chat?.id || callbackQuery.from?.id;

	// Validación básica de seguridad por chat ID si está configurado
	if (env.TELEGRAM_CHAT_ID && fromId !== String(env.TELEGRAM_CHAT_ID) && String(chatId) !== String(env.TELEGRAM_CHAT_ID)) {
		await answerCallbackQuery(env, callbackId, "⛔ No estás autorizado para realizar esta acción.", true);
		return Response.json({ ok: false, error: "Unauthorized" }, { status: 403 });
	}

	if (!data || data === "noop") {
		await answerCallbackQuery(env, callbackId, "ℹ️ Esta acción ya fue completada.");
		return Response.json({ ok: true, message: "Noop" });
	}

	if (data.startsWith("send:")) {
		const parts = data.split(":");
		const subscriptionId = parseInt(parts[1], 10);

		if (isNaN(subscriptionId)) {
			await answerCallbackQuery(env, callbackId, "❌ ID de suscripción inválido.", true);
			return Response.json({ ok: false, error: "Invalid subscription ID" });
		}

		// 1. Obtener la suscripción y datos del cliente
		const sub = await getSubscriptionById(env, subscriptionId);
		if (!sub) {
			await answerCallbackQuery(env, callbackId, "❌ Suscripción no encontrada en la base de datos.", true);
			return Response.json({ ok: false, error: "Subscription not found" });
		}

		if (!sub.clientEmail) {
			await answerCallbackQuery(
				env,
				callbackId,
				`⚠️ ${sub.clientName} no tiene correo electrónico registrado.`,
				true
			);
			return Response.json({ ok: false, error: "Client has no email" });
		}

		// 2. Enviar correo vía Resend
		const emailResult = await sendRenewalEmail(env, sub);
		const stage = `reminder_${sub.daysRemaining}d_client`;
		const notificationType =
			sub.daysRemaining === 0 ? "RENEWAL_REMINDER_DUE" : `RENEWAL_REMINDER_${sub.daysRemaining}D`;

		// 3. Registrar auditoría en notification_log
		await logNotification(env, {
			type: notificationType,
			recipientEmail: sub.clientEmail,
			payload: {
				subscriptionId: sub.uuid,
				clientName: sub.clientName,
				serviceName: sub.serviceName,
				paymentDueDate: sub.paymentDueDate,
				daysRemaining: sub.daysRemaining,
			},
			status: emailResult.ok ? "sent" : "failed",
			entityType: "subscription",
			entityId: sub.id,
			stage,
		});

		if (!emailResult.ok) {
			await answerCallbackQuery(
				env,
				callbackId,
				`❌ Error al enviar correo: ${emailResult.error}`,
				true
			);
			return Response.json({ ok: false, error: emailResult.error });
		}

		// 4. Confirmar a Telegram con popup toast
		await answerCallbackQuery(
			env,
			callbackId,
			`✅ Correo enviado a ${sub.clientEmail} (${sub.serviceName})`
		);

		// 5. Actualizar el teclado inline para marcar el botón como enviado
		if (message && message.reply_markup && message.reply_markup.inline_keyboard) {
			const currentMarkup = message.reply_markup as InlineKeyboardMarkup;
			const updatedKeyboard = currentMarkup.inline_keyboard.map((row) =>
				row.map((btn) => {
					if (btn.callback_data === data) {
						return {
							text: `✅ ${sub.clientName} (Correo Enviado)`,
							callback_data: "noop",
						};
					}
					return btn;
				})
			);

			await editMessageReplyMarkup(env, chatId, message.message_id, {
				inline_keyboard: updatedKeyboard,
			});
		}

		return Response.json({ ok: true, emailSent: true });
	}

	await answerCallbackQuery(env, callbackId, "Acción no reconocida.");
	return Response.json({ ok: true });
}

export default {
	// ⏰ Cron Trigger: se ejecuta automáticamente todos los días a las 8:00 AM (hora de Guatemala / 14:00 UTC)
	async scheduled(controller, env, ctx): Promise<void> {
		ctx.waitUntil(runDailyReminder(env));
	},

	// 🌐 Handler HTTP: atiende Webhooks de Telegram y llamadas manuales / health checks
	async fetch(request, env, ctx): Promise<Response> {
		const url = new URL(request.url);

		if (url.pathname === "/favicon.ico") {
			return new Response(null, { status: 204 });
		}

		// 📲 Manejo de Webhooks POST desde Telegram (clic en botones inline)
		if (request.method === "POST") {
			try {
				const body = (await request.json()) as any;
				if (body && body.callback_query) {
					return await handleTelegramCallbackQuery(env, body.callback_query);
				}
				// Si es otro tipo de actualización de Telegram (mensaje, etc.)
				return Response.json({ ok: true });
			} catch (err: any) {
				return Response.json(
					{ ok: false, error: err.message || "Error procesando webhook" },
					{ status: 400 }
				);
			}
		}

		// 🔒 Validación de seguridad para llamadas HTTP GET de prueba
		if (env.CRON_SECRET) {
			const providedSecret =
				url.searchParams.get("secret") ||
				request.headers.get("x-cron-secret") ||
				request.headers.get("authorization")?.replace(/^Bearer\s+/i, "");

			if (providedSecret !== env.CRON_SECRET) {
				return Response.json(
					{ ok: false, error: "Unauthorized" },
					{ status: 401 }
				);
			}
		}

		try {
			const targetDate = url.searchParams.get("date") || undefined;
			const result = await runDailyReminder(env, targetDate);
			return Response.json({
				ok: true,
				...result,
			});
		} catch (error: any) {
			return Response.json(
				{
					ok: false,
					error: error.message || "Error procesando la solicitud",
				},
				{ status: 500 }
			);
		}
	},
} satisfies ExportedHandler<Env>;
