export interface Env {
	RESEND_API_KEY: string;
	SUPABASE_URL: string;
	SUPABASE_SERVICE_ROLE_KEY: string;
	WEBHOOK_SECRET?: string;
	FROM_EMAIL: string;
}

export interface NotificationLogRow {
	id: number;
	uuid: string;
	type: string;
	recipient_email: string;
	payload: string | null;
	status: 'pending' | 'sent' | 'failed';
	entity_type?: string | null;
	entity_id?: number | null;
	stage?: string | null;
	error_message?: string | null;
	created_at: string;
}

export interface SupabaseWebhookPayload {
	type: 'INSERT' | 'UPDATE' | 'DELETE';
	table: string;
	schema: string;
	record: NotificationLogRow;
	old_record: NotificationLogRow | null;
}
