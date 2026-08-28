import { FormattedSubscription, RenewalWindowGroup } from "../types/subscription";

export interface InlineKeyboardButton {
	text: string;
	callback_data: string;
}

export interface InlineKeyboardMarkup {
	inline_keyboard: InlineKeyboardButton[][];
}

/**
 * Realiza una llamada a la API de Telegram.
 */
async function callTelegramApi(
	env: Env,
	method: string,
	body: Record<string, any>
) {
	if (!env.TELEGRAM_BOT_TOKEN) {
		throw new Error("Falta la variable TELEGRAM_BOT_TOKEN en el entorno.");
	}

	const url = `https://api.telegram.org/bot${env.TELEGRAM_BOT_TOKEN}/${method}`;
	const response = await fetch(url, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(body),
	});

	if (!response.ok) {
		const errText = await response.text();
		throw new Error(`Telegram API error on ${method} (${response.status}): ${errText}`);
	}

	return response.json();
}

/**
 * Envía un mensaje de texto formateado a Telegram (con soporte opcional de teclado inline).
 */
export async function sendTelegramMessage(
	env: Env,
	text: string,
	replyMarkup?: InlineKeyboardMarkup,
	chatId?: string | number
) {
	const targetChatId = chatId || env.TELEGRAM_CHAT_ID;
	if (!targetChatId) {
		throw new Error("Falta la variable TELEGRAM_CHAT_ID en el entorno.");
	}

	const body: Record<string, any> = {
		chat_id: targetChatId,
		text,
		parse_mode: "Markdown",
	};

	if (replyMarkup && replyMarkup.inline_keyboard.length > 0) {
		body.reply_markup = replyMarkup;
	}

	return callTelegramApi(env, "sendMessage", body);
}

/**
 * Responde a un callback_query de Telegram (muestra notificación toast o alerta al usuario en la app).
 */
export async function answerCallbackQuery(
	env: Env,
	callbackQueryId: string,
	text?: string,
	showAlert: boolean = false
) {
	return callTelegramApi(env, "answerCallbackQuery", {
		callback_query_id: callbackQueryId,
		text,
		show_alert: showAlert,
	});
}

/**
 * Actualiza los botones de un mensaje ya enviado en Telegram.
 */
export async function editMessageReplyMarkup(
	env: Env,
	chatId: string | number,
	messageId: number,
	replyMarkup?: InlineKeyboardMarkup
) {
	return callTelegramApi(env, "editMessageReplyMarkup", {
		chat_id: chatId,
		message_id: messageId,
		reply_markup: replyMarkup || { inline_keyboard: [] },
	});
}

/**
 * Construye el reporte consolidado y los botones de acción para las suscripciones por vencer.
 */
export function buildConsolidatedReport(groups: RenewalWindowGroup[]): {
	text: string;
	replyMarkup?: InlineKeyboardMarkup;
	totalSubscriptions: number;
} {
	const totalSubscriptions = groups.reduce((acc, g) => acc + g.subscriptions.length, 0);

	if (totalSubscriptions === 0) {
		return {
			text: "ℹ️ *Reporte de suscripciones*\n\nNo hay suscripciones que venzan hoy ni en los próximos 3 días.",
			totalSubscriptions: 0,
		};
	}

	let message = "🔔 *Reporte de Renovaciones de Clientes*\n──────────────────\n\n";
	const inlineKeyboard: InlineKeyboardButton[][] = [];

	for (const group of groups) {
		if (group.subscriptions.length === 0) continue;

		message += `*${group.label}* (${group.subscriptions.length})\n`;

		group.subscriptions.forEach((sub, idx) => {
			message += `*${idx + 1}. ${sub.clientName}*\n`;
			message += `  📺 *Servicio:* ${sub.serviceName}${sub.profileName ? ` (${sub.profileName})` : ""}\n`;
			if (sub.clientEmail) {
				message += `  📧 *Email:* \`${sub.clientEmail}\`\n`;
			}
			if (sub.clientPhone) {
				message += `  📞 *Teléfono:* \`${sub.clientPhone}\`\n`;
			}
			if (sub.price) {
				message += `  💰 *Precio:* Q ${sub.price}\n`;
			}
			message += `  📅 *Vence:* \`${sub.paymentDueDate}\`\n\n`;

			// Crear botón inline para enviar recordatorio por correo
			const dayTag = sub.daysRemaining === 0 ? "Hoy" : `${sub.daysRemaining}d`;
			const buttonText = sub.clientEmail
				? `📧 Enviar a ${sub.clientName} (${dayTag})`
				: `⚠️ ${sub.clientName} (Sin email)`;

			// Si el cliente no tiene email, el botón puede mostrar alerta en el callback
			const callbackData = `send:${sub.id}:${sub.daysRemaining}`;

			inlineKeyboard.push([
				{
					text: buttonText,
					callback_data: callbackData,
				},
			]);
		});
	}

	message += `──────────────────\n📊 *Total:* ${totalSubscriptions} suscripción(es) encontrada(s).\n_Presiona un botón para enviar el correo al cliente._`;

	return {
		text: message,
		replyMarkup: { inline_keyboard: inlineKeyboard },
		totalSubscriptions,
	};
}

/**
 * Orquesta la construcción y el envío del reporte consolidado a Telegram.
 */
export async function sendExpiringSubscriptionsReport(
	env: Env,
	date: string,
	subscriptions: FormattedSubscription[]
) {
	// Mapeo retrocompatible si se llama con lista plana de suscripciones
	const defaultGroup: RenewalWindowGroup[] = [
		{
			days: 0,
			label: `🔴 Vencimientos (${date})`,
			subscriptions,
		},
	];

	const { text, replyMarkup } = buildConsolidatedReport(defaultGroup);
	return sendTelegramMessage(env, text, replyMarkup);
}
