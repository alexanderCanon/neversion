import {
	FormattedSubscription,
	RenewalWindowGroup,
	MasterAccountWindowGroup,
} from "../types/subscription";

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
	const safeText = text ? text.slice(0, 195) : undefined;
	return callTelegramApi(env, "answerCallbackQuery", {
		callback_query_id: callbackQueryId,
		text: safeText,
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
 * Construye el reporte consolidado y los botones de acción para las suscripciones y cuentas maestras por vencer.
 */
export function buildConsolidatedReport(
	subscriptionGroups: RenewalWindowGroup[],
	accountGroups: MasterAccountWindowGroup[] = []
): {
	text: string;
	replyMarkup?: InlineKeyboardMarkup;
	totalSubscriptions: number;
	totalAccounts: number;
} {
	const totalSubscriptions = subscriptionGroups.reduce((acc, g) => acc + g.subscriptions.length, 0);
	const totalAccounts = accountGroups.reduce((acc, g) => acc + g.accounts.length, 0);

	if (totalSubscriptions === 0 && totalAccounts === 0) {
		const emptyText =
			accountGroups.length > 0
				? "ℹ️ *Reporte diario Neversion*\n\nNo hay renovaciones de clientes ni cuentas maestras que venzan hoy o en las próximas ventanas."
				: "ℹ️ *Reporte de suscripciones*\n\nNo hay suscripciones que venzan hoy ni en los próximos 3 días.";

		return {
			text: emptyText,
			totalSubscriptions: 0,
			totalAccounts: 0,
		};
	}

	const sections: string[] = [];
	const inlineKeyboard: InlineKeyboardButton[][] = [];

	// Sección 1: Renovaciones de Clientes
	if (totalSubscriptions > 0) {
		let clientMessage = "🔔 *Reporte de Renovaciones de Clientes*\n──────────────────\n";

		for (const group of subscriptionGroups) {
			if (group.subscriptions.length === 0) continue;

			clientMessage += `\n*${group.label}* (${group.subscriptions.length})\n`;

			group.subscriptions.forEach((sub, idx) => {
				clientMessage += `*${idx + 1}. ${sub.clientName}*\n`;
				clientMessage += `  📺 *Servicio:* ${sub.serviceName}${sub.profileName ? ` (${sub.profileName})` : ""}\n`;
				if (sub.clientEmail) {
					clientMessage += `  📧 *Email:* \`${sub.clientEmail}\`\n`;
				}
				if (sub.clientPhone) {
					clientMessage += `  📞 *Teléfono:* \`${sub.clientPhone}\`\n`;
				}
				if (sub.price) {
					clientMessage += `  💰 *Precio:* Q ${sub.price}\n`;
				}
				clientMessage += `  📅 *Vence:* \`${sub.paymentDueDate}\`\n`;

				const dayTag = sub.daysRemaining === 0 ? "Hoy" : `${sub.daysRemaining}d`;
				const buttonText = sub.clientEmail
					? `📧 Enviar a ${sub.clientName} (${dayTag})`
					: `⚠️ ${sub.clientName} (Sin email)`;

				const callbackData = `send:${sub.id}:${sub.daysRemaining}`;

				inlineKeyboard.push([
					{
						text: buttonText,
						callback_data: callbackData,
					},
				]);
			});
		}

		sections.push(clientMessage);
	}

	// Sección 2: Cuentas Maestras por Renovar
	if (totalAccounts > 0) {
		let accountMessage = "🔑 *Cuentas Maestras por Renovar*\n──────────────────\n";

		for (const group of accountGroups) {
			if (group.accounts.length === 0) continue;

			accountMessage += `\n*${group.label}* (${group.accounts.length})\n`;

			group.accounts.forEach((acc, idx) => {
				accountMessage += `*${idx + 1}. ${acc.serviceName}*\n`;
				accountMessage += `  📧 *Email:* \`${acc.email}\`\n`;
				accountMessage += `  🏢 *Proveedor:* \`${acc.source || "No especificado"}\`\n`;
				accountMessage += `  📅 *Vence:* \`${acc.renewalDate}\`\n`;
			});
		}

		sections.push(accountMessage);
	}

	// Footer con totales
	let footer = "──────────────────\n";
	const totals: string[] = [];
	if (totalSubscriptions > 0) {
		totals.push(`${totalSubscriptions} suscripción(es)`);
	}
	if (totalAccounts > 0) {
		totals.push(`${totalAccounts} cuenta(s) maestra(s)`);
	}
	footer += `📊 *Total:* ${totals.join(" | ")}.`;

	if (inlineKeyboard.length > 0) {
		footer += "\n_Presiona un botón para enviar el correo al cliente._";
	}

	sections.push(footer);

	return {
		text: sections.join("\n\n"),
		replyMarkup: inlineKeyboard.length > 0 ? { inline_keyboard: inlineKeyboard } : undefined,
		totalSubscriptions,
		totalAccounts,
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
