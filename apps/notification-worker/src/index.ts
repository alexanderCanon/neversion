import { getTemplateConfig } from './templates';
import { sendTemplateEmail } from './resend';
import { getSupabaseClient, updateNotificationStatus, fetchPendingNotifications } from './supabase';
import type { Env, NotificationLogRow, SupabaseWebhookPayload } from './types';

export default {
	async fetch(request: Request, env: Env): Promise<Response> {
		const url = new URL(request.url);

		// 1. Health Probe
		if (request.method === 'GET' && (url.pathname === '/' || url.pathname === '/health')) {
			return new Response(JSON.stringify({ status: 'ok', service: 'notification-worker' }), {
				headers: { 'Content-Type': 'application/json' },
			});
		}

		// 2. Webhook Authentication
		if (env.WEBHOOK_SECRET) {
			const providedSecret =
				request.headers.get('x-webhook-secret') ||
				request.headers.get('authorization')?.replace('Bearer ', '');

			if (providedSecret !== env.WEBHOOK_SECRET) {
				return new Response(JSON.stringify({ error: 'Unauthorized: invalid webhook secret' }), {
					status: 401,
					headers: { 'Content-Type': 'application/json' },
				});
			}
		}

		// 3. Database Webhook Endpoint (POST /webhook)
		if (request.method === 'POST' && url.pathname === '/webhook') {
			try {
				const body = (await request.json()) as Partial<SupabaseWebhookPayload> & Partial<NotificationLogRow>;
				const record: NotificationLogRow | undefined = body.record || (body.id && body.type ? (body as NotificationLogRow) : undefined);

				if (!record || !record.id || !record.type) {
					return new Response(JSON.stringify({ error: 'Bad Request: missing notification record' }), {
						status: 400,
						headers: { 'Content-Type': 'application/json' },
					});
				}

				const result = await processNotificationRecord(record, env);
				return new Response(JSON.stringify(result), {
					status: result.success ? 200 : 422,
					headers: { 'Content-Type': 'application/json' },
				});
			} catch (err: unknown) {
				const msg = err instanceof Error ? err.message : 'Invalid JSON payload';
				return new Response(JSON.stringify({ error: msg }), {
					status: 400,
					headers: { 'Content-Type': 'application/json' },
				});
			}
		}

		// 4. Retry / Process Pending Endpoint (POST /retry-pending)
		if (request.method === 'POST' && url.pathname === '/retry-pending') {
			const supabase = getSupabaseClient(env);
			const pendingList = await fetchPendingNotifications(supabase, 20);
			const results = [];

			for (const record of pendingList) {
				const result = await processNotificationRecord(record, env);
				results.push({ id: record.id, type: record.type, ...result });
			}

			return new Response(JSON.stringify({ processed: results.length, details: results }), {
				headers: { 'Content-Type': 'application/json' },
			});
		}

		return new Response(JSON.stringify({ error: 'Not Found' }), {
			status: 404,
			headers: { 'Content-Type': 'application/json' },
		});
	},

	/**
	 * Scheduled handler (optional Cloudflare Cron Trigger) for guaranteed delivery of pending records.
	 */
	async scheduled(_controller: ScheduledController, env: Env, _ctx: ExecutionContext): Promise<void> {
		const supabase = getSupabaseClient(env);
		const pendingList = await fetchPendingNotifications(supabase, 20);
		for (const record of pendingList) {
			await processNotificationRecord(record, env);
		}
	},
} satisfies ExportedHandler<Env>;

/**
 * Processes an individual notification_log record, dispatches email, and updates DB status.
 */
async function processNotificationRecord(
	record: NotificationLogRow,
	env: Env
): Promise<{ success: boolean; error?: string; emailId?: string }> {
	const supabase = getSupabaseClient(env);

	// Validate recipient email
	if (!record.recipient_email || record.recipient_email.trim() === '') {
		await updateNotificationStatus(supabase, record.id, 'failed', 'Missing recipient email address');
		return { success: false, error: 'Missing recipient email address' };
	}

	// Resolve Resend template
	const templateConfig = getTemplateConfig(record.type);
	if (!templateConfig) {
		const errorMsg = `No template mapped for event type: ${record.type}`;
		await updateNotificationStatus(supabase, record.id, 'failed', errorMsg);
		return { success: false, error: errorMsg };
	}

	// Parse template dynamic data
	let templateData: Record<string, unknown> = {};
	if (record.payload) {
		try {
			templateData = JSON.parse(record.payload);
		} catch {
			templateData = { rawPayload: record.payload };
		}
	}

	// Dispatch email via Resend
	const sendResult = await sendTemplateEmail({
		apiKey: env.RESEND_API_KEY,
		from: env.FROM_EMAIL,
		to: record.recipient_email,
		subject: templateConfig.defaultSubject,
		templateId: templateConfig.id,
		templateData,
	});

	if (sendResult.success) {
		await updateNotificationStatus(supabase, record.id, 'sent');
		return { success: true, emailId: sendResult.emailId };
	} else {
		await updateNotificationStatus(supabase, record.id, 'failed', sendResult.error);
		return { success: false, error: sendResult.error };
	}
}
