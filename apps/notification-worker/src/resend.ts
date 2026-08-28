export interface SendTemplateEmailParams {
	apiKey: string;
	from: string;
	to: string;
	subject: string;
	templateId: string;
	templateData: Record<string, unknown>;
}

export interface SendEmailResult {
	success: boolean;
	emailId?: string;
	error?: string;
}

/**
 * Dispatches an email using Resend Template ID and dynamic template data.
 */
export async function sendTemplateEmail(params: SendTemplateEmailParams): Promise<SendEmailResult> {
	try {
		const response = await fetch('https://api.resend.com/emails', {
			method: 'POST',
			headers: {
				Authorization: `Bearer ${params.apiKey}`,
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
				from: params.from,
				to: params.to,
				subject: params.subject,
				template: {
					id: params.templateId,
					data: params.templateData,
				},
			}),
		});

		const data = (await response.json()) as { id?: string; message?: string; name?: string };

		if (!response.ok) {
			const errorMsg = data.message || `Resend HTTP error ${response.status}`;
			return { success: false, error: errorMsg };
		}

		return { success: true, emailId: data.id };
	} catch (err: unknown) {
		const msg = err instanceof Error ? err.message : 'Unknown network error sending email';
		return { success: false, error: msg };
	}
}
