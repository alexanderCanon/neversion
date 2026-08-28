import { createClient } from "@supabase/supabase-js";
import { FormattedSubscription, RenewalWindowGroup } from "../types/subscription";
import { firstObj } from "../utils/helpers";
import { getGuatemalaDate } from "../utils/date";

export function getSupabaseClient(env: Env) {
	if (!env.SUPABASE_URL || !env.SUPABASE_SERVICE_ROLE_KEY) {
		throw new Error("Faltan las variables SUPABASE_URL o SUPABASE_SERVICE_ROLE_KEY en el entorno.");
	}
	return createClient(env.SUPABASE_URL, env.SUPABASE_SERVICE_ROLE_KEY);
}

function mapSubscription(sub: any, daysRemaining: number): FormattedSubscription {
	const client = firstObj(sub.clients);
	const profile = firstObj(sub.profiles);
	const account = firstObj(profile?.accounts);
	const accountService = firstObj(account?.services);
	const directService = firstObj(sub.services);

	const serviceName = directService?.name || accountService?.name || "Servicio no especificado";

	return {
		id: sub.id,
		uuid: sub.uuid,
		clientName: client?.name || "Cliente sin nombre",
		clientEmail: client?.email || undefined,
		clientPhone: client?.phone || undefined,
		serviceName,
		profileName: profile?.name || undefined,
		paymentDueDate: sub.payment_due_date,
		price: sub.price_sold,
		daysRemaining,
	};
}

/**
 * Consulta Supabase para obtener las suscripciones activas que vencen en una fecha específica.
 */
export async function getExpiringSubscriptions(
	env: Env,
	targetDate: string
): Promise<FormattedSubscription[]> {
	const supabase = getSupabaseClient(env);

	const { data: rawSubs, error } = await supabase
		.from("subscriptions")
		.select(`
			id,
			uuid,
			payment_due_date,
			status,
			price_sold,
			notes,
			clients ( id, name, email, phone ),
			services ( id, name ),
			profiles (
				id,
				name,
				accounts (
					id,
					email,
					services ( id, name )
				)
			)
		`)
		.in("status", ["active", "ACTIVE"])
		.eq("payment_due_date", targetDate);

	if (error) {
		throw new Error(`Error consultando Supabase: ${error.message}`);
	}

	return (rawSubs || []).map((s: any) => mapSubscription(s, 0));
}

/**
 * Consulta Supabase para obtener las suscripciones activas agrupadas por ventanas (3 días, 1 día, hoy).
 */
export async function getExpiringSubscriptionsByWindows(
	env: Env,
	baseDate?: string
): Promise<RenewalWindowGroup[]> {
	const supabase = getSupabaseClient(env);

	// Si se pasa baseDate (para pruebas), calculamos las fechas objetivo a partir de baseDate
	const getTargetDateStr = (days: number): string => {
		if (baseDate) {
			const d = new Date(`${baseDate}T12:00:00Z`);
			d.setUTCDate(d.getUTCDate() + days);
			return d.toISOString().split("T")[0];
		}
		return getGuatemalaDate(days);
	};

	const windows = [
		{ days: 3, label: "🟡 Vencen en 3 días" },
		{ days: 1, label: "🟠 Vencen mañana (1 día)" },
		{ days: 0, label: "🔴 Vencen hoy" },
	];

	const targetDates = windows.map((w) => getTargetDateStr(w.days));

	const { data: rawSubs, error } = await supabase
		.from("subscriptions")
		.select(`
			id,
			uuid,
			payment_due_date,
			status,
			price_sold,
			notes,
			clients ( id, name, email, phone ),
			services ( id, name ),
			profiles (
				id,
				name,
				accounts (
					id,
					email,
					services ( id, name )
				)
			)
		`)
		.in("status", ["active", "ACTIVE"])
		.in("payment_due_date", targetDates);

	if (error) {
		throw new Error(`Error consultando Supabase: ${error.message}`);
	}

	const allSubs = rawSubs || [];

	return windows.map((win) => {
		const targetStr = getTargetDateStr(win.days);
		const subsForWindow = allSubs
			.filter((s: any) => s.payment_due_date === targetStr)
			.map((s: any) => mapSubscription(s, win.days));

		return {
			days: win.days,
			label: win.label,
			subscriptions: subsForWindow,
		};
	});
}

/**
 * Obtiene una suscripción individual por ID.
 */
export async function getSubscriptionById(
	env: Env,
	subscriptionId: number
): Promise<FormattedSubscription | null> {
	const supabase = getSupabaseClient(env);

	const { data: sub, error } = await supabase
		.from("subscriptions")
		.select(`
			id,
			uuid,
			payment_due_date,
			status,
			price_sold,
			notes,
			clients ( id, name, email, phone ),
			services ( id, name ),
			profiles (
				id,
				name,
				accounts (
					id,
					email,
					services ( id, name )
				)
			)
		`)
		.eq("id", subscriptionId)
		.maybeSingle();

	if (error || !sub) {
		return null;
	}

	// Calcular días restantes respecto a hoy en Guatemala
	const todayStr = getGuatemalaDate(0);
	const dueDateRaw = String(sub.payment_due_date || "").split("T")[0];
	const todayDate = new Date(`${todayStr}T12:00:00Z`);
	const dueDate = new Date(`${dueDateRaw}T12:00:00Z`);
	const diffTime = dueDate.getTime() - todayDate.getTime();
	const daysRemaining = isNaN(diffTime) ? 0 : Math.round(diffTime / (1000 * 60 * 60 * 24));

	return mapSubscription(sub, daysRemaining);
}

/**
 * Registra una entrada en notification_log para auditoría de envíos.
 */
export async function logNotification(
	env: Env,
	entry: {
		type: string;
		recipientEmail: string;
		payload: Record<string, any>;
		status: "sent" | "failed";
		entityType: "subscription" | "account";
		entityId: number;
		stage: string;
	}
): Promise<void> {
	const supabase = getSupabaseClient(env);

	const { error } = await supabase.from("notification_log").insert({
		type: entry.type,
		recipient_email: entry.recipientEmail,
		payload: JSON.stringify(entry.payload),
		status: entry.status,
		entity_type: entry.entityType,
		entity_id: entry.entityId,
		stage: entry.stage,
		processed_at: new Date().toISOString(),
	});

	if (error) {
		console.error("Error insertando en notification_log:", error);
	}
}
