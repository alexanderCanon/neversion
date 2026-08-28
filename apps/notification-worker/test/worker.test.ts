import { describe, it, expect, vi, beforeEach } from 'vitest';
import worker from '../src/index';
import { getTemplateConfig, TEMPLATE_CONFIG } from '../src/templates';
import type { Env } from '../src/types';

const mockEnv: Env = {
	RESEND_API_KEY: 're_test_key',
	SUPABASE_URL: 'https://test.supabase.co',
	SUPABASE_SERVICE_ROLE_KEY: 'test_service_role_key',
	WEBHOOK_SECRET: 'test_webhook_secret',
	FROM_EMAIL: 'Neversion <noreply@mail.neversion.com>',
};

describe('Notification Worker — Templates Mapping', () => {
	it('should resolve template IDs for all core events', () => {
		const expectedEvents = [
			'ACCESS_DELIVERED',
			'ACCESS_REVOKED',
			'SUBSCRIPTION_RENEWED',
			'SUBSCRIPTION_EXPIRED',
			'PAYMENT_APPROVED',
			'RECEIPT_UPLOADED',
			'VENDOR_RECEIPT_UPLOADED',
			'RECEIPT_REJECTED',
			'CLIENT_WELCOME',
			'CLIENT_REGISTRATION',
			'VENDOR_WELCOME',
			'ORDER_COMPLETED',
			'ORDER_CANCELLED',
		];

		for (const eventType of expectedEvents) {
			const config = getTemplateConfig(eventType);
			expect(config, `Missing template for event: ${eventType}`).not.toBeNull();
			expect(config?.id).toBeDefined();
			expect(config?.defaultSubject).toBeDefined();
		}
	});

	it('should return null for unknown event type', () => {
		expect(getTemplateConfig('UNKNOWN_EVENT_TYPE')).toBeNull();
	});
});

describe('Notification Worker — HTTP Routes', () => {
	it('should return 200 on health check GET /', async () => {
		const request = new Request('http://localhost/', { method: 'GET' });
		const response = await worker.fetch(request, mockEnv);
		expect(response.status).toBe(200);

		const data = (await response.json()) as { status: string };
		expect(data.status).toBe('ok');
	});

	it('should reject unauthorized webhook requests when WEBHOOK_SECRET is set', async () => {
		const request = new Request('http://localhost/webhook', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ type: 'INSERT' }),
		});

		const response = await worker.fetch(request, mockEnv);
		expect(response.status).toBe(401);
	});

	it('should return 400 when webhook record is missing or malformed', async () => {
		const request = new Request('http://localhost/webhook', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'x-webhook-secret': 'test_webhook_secret',
			},
			body: JSON.stringify({ type: 'INSERT', table: 'notification_log' }),
		});

		const response = await worker.fetch(request, mockEnv);
		expect(response.status).toBe(400);
	});
});
