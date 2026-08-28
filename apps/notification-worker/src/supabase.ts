import { createClient, type SupabaseClient } from '@supabase/supabase-js';
import type { Env, NotificationLogRow } from './types';

export function getSupabaseClient(env: Env): SupabaseClient {
	return createClient(env.SUPABASE_URL, env.SUPABASE_SERVICE_ROLE_KEY, {
		auth: {
			persistSession: false,
			autoRefreshToken: false,
		},
	});
}

/**
 * Updates a notification_log row status to 'sent' or 'failed'.
 */
export async function updateNotificationStatus(
	supabase: SupabaseClient,
	id: number,
	status: 'sent' | 'failed',
	errorMessage?: string
): Promise<{ success: boolean; error?: string }> {
	try {
		const updateData: Record<string, unknown> = {
			status,
			processed_at: new Date().toISOString(),
			error_message: errorMessage || null,
		};

		const { error } = await supabase
			.from('notification_log')
			.update(updateData)
			.eq('id', id);

		if (error) {
			return { success: false, error: error.message };
		}

		return { success: true };
	} catch (err: unknown) {
		const msg = err instanceof Error ? err.message : 'Unknown database error';
		return { success: false, error: msg };
	}
}

/**
 * Fetches pending notification_log records for fallback/retry processing.
 */
export async function fetchPendingNotifications(
	supabase: SupabaseClient,
	limit = 20
): Promise<NotificationLogRow[]> {
	const { data, error } = await supabase
		.from('notification_log')
		.select('*')
		.eq('status', 'pending')
		.order('created_at', { ascending: true })
		.limit(limit);

	if (error || !data) {
		return [];
	}

	return data as NotificationLogRow[];
}
