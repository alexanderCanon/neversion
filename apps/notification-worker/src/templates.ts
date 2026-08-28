export interface TemplateConfig {
	id: string;
	defaultSubject: string;
}

/**
 * Resend Template IDs mapped from event types in notification_log.
 * These match published templates in the Neversion Resend account.
 */
export const TEMPLATE_CONFIG: Record<string, TemplateConfig> = {
	ACCESS_DELIVERED: {
		id: 'ad8eb4d1-1336-496f-b5a6-2a261a91d06f',
		defaultSubject: '¡Tus accesos están listos!',
	},
	ACCESS_REVOKED: {
		id: '624822e4-c948-4bfe-9339-ea28a38f5732',
		defaultSubject: 'Acceso revocado',
	},
	SUBSCRIPTION_RENEWED: {
		id: '1eb90c1e-a8c8-4f9e-b780-5c7c0674f4b7',
		defaultSubject: '¡Renovación Confirmada!',
	},
	SUBSCRIPTION_EXPIRED: {
		id: '518bb9ee-31e1-4f37-8d92-675c6139bfa8',
		defaultSubject: 'Tu suscripción ha vencido',
	},
	PAYMENT_APPROVED: {
		id: 'f4a0da61-44c5-423b-8126-57d53f5c72cf',
		defaultSubject: '¡Tu pago ha sido aprobado!',
	},
	RECEIPT_UPLOADED: {
		id: '6fb18dc5-55dd-46c6-9d7d-69e362a1874c',
		defaultSubject: 'Comprobante recibido',
	},
	VENDOR_RECEIPT_UPLOADED: {
		id: '5b19be95-a13e-4f15-af3c-0be0fd39df2b',
		defaultSubject: 'Nuevo comprobante de pago recibido',
	},
	RECEIPT_REJECTED: {
		id: 'a95c9368-2649-41e9-9f72-94881538c6ac',
		defaultSubject: 'Comprobante rechazado',
	},
	CLIENT_WELCOME: {
		id: '064afad2-040f-455b-bb5e-4db3d2b143bc',
		defaultSubject: '¡Bienvenido a Neversion!',
	},
	CLIENT_REGISTRATION: {
		id: '064afad2-040f-455b-bb5e-4db3d2b143bc',
		defaultSubject: '¡Bienvenido a Neversion!',
	},
	VENDOR_WELCOME: {
		id: 'c0d9f337-d8e3-47c3-9a5a-420b2bf3c733',
		defaultSubject: '¡Bienvenido a Neversion, Vendedor!',
	},
	ORDER_COMPLETED: {
		id: '62ed6cea-2566-4d8d-9505-622b301961fa',
		defaultSubject: 'Tu orden ha sido completada',
	},
	ORDER_CANCELLED: {
		id: '60f18ad5-09f0-4f55-886a-e87398630f2e',
		defaultSubject: 'Tu orden ha sido cancelada',
	},
	MANUAL_PAYMENT_REMINDER: {
		id: '3f873c32-601f-4cfd-ba9e-072d2b838b8e',
		defaultSubject: 'Recordatorio de pago de suscripción',
	},
};

export function getTemplateConfig(eventType: string): TemplateConfig | null {
	return TEMPLATE_CONFIG[eventType] ?? null;
}
