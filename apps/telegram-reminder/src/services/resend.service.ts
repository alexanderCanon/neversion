import { FormattedSubscription } from "../types/subscription";

/**
 * Genera el asunto del correo en función de los días restantes.
 */
function getEmailSubject(serviceName: string, daysRemaining: number): string {
	if (daysRemaining === 0) {
		return `🔔 Tu suscripción a ${serviceName} vence hoy`;
	}
	if (daysRemaining === 1) {
		return `⏰ Tu suscripción a ${serviceName} vence mañana`;
	}
	return `📅 Recordatorio: Tu suscripción a ${serviceName} vence en ${daysRemaining} días`;
}

/**
 * Genera la plantilla HTML para el correo de renovación.
 */
function buildRenewalEmailHtml(sub: FormattedSubscription): string {
	const dueNotice =
		sub.daysRemaining === 0
			? "vence <strong>hoy</strong>"
			: sub.daysRemaining === 1
			? "vence <strong>mañana</strong>"
			: `vence en <strong>${sub.daysRemaining} días</strong>`;

	return `
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <title>Recordatorio de Renovación - Neversion</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; color: #1e293b; margin: 0; padding: 20px; }
    .container { max-width: 540px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; }
    .header { background: #0f172a; color: #ffffff; padding: 24px; text-align: center; }
    .header h1 { margin: 0; font-size: 20px; font-weight: 700; letter-spacing: -0.5px; }
    .body { padding: 24px; }
    .details-box { background: #f1f5f9; border-radius: 8px; padding: 16px; margin: 20px 0; }
    .details-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
    .details-row:last-child { margin-bottom: 0; }
    .label { color: #64748b; }
    .value { font-weight: 600; color: #0f172a; }
    .footer { padding: 16px 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>Neversion</h1>
    </div>
    <div class="body">
      <p>Hola <strong>${sub.clientName}</strong>,</p>
      <p>Te recordamos que tu suscripción a <strong>${sub.serviceName}</strong> ${dueNotice} (<code>${sub.paymentDueDate}</code>).</p>
      
      <div class="details-box">
        <div class="details-row">
          <span class="label">Servicio:</span>
          <span class="value">${sub.serviceName}${sub.profileName ? ` (${sub.profileName})` : ""}</span>
        </div>
        <div class="details-row">
          <span class="label">Fecha de vencimiento:</span>
          <span class="value">${sub.paymentDueDate}</span>
        </div>
        ${sub.price ? `
        <div class="details-row">
          <span class="label">Monto de renovación:</span>
          <span class="value">Q ${sub.price}</span>
        </div>` : ""}
      </div>

      <p>Para asegurar la continuidad de tu servicio sin interrupciones, por favor comunícate con nosotros para coordinar tu pago.</p>
      <p>¡Gracias por tu preferencia!</p>
    </div>
    <div class="footer">
      <p>Este es un mensaje automático de Neversion.</p>
    </div>
  </div>
</body>
</html>
`.trim();
}

/**
 * Envía el correo de renovación al cliente a través de Resend.
 */
export async function sendRenewalEmail(
	env: Env,
	subscription: FormattedSubscription
): Promise<{ ok: boolean; id?: string; error?: string }> {
	if (!env.RESEND_API_KEY) {
		return { ok: false, error: "RESEND_API_KEY no está configurada en el entorno." };
	}

	if (!subscription.clientEmail) {
		return { ok: false, error: `El cliente ${subscription.clientName} no tiene correo electrónico registrado.` };
	}

	const sender = env.SENDER_EMAIL || "Neversion <notificaciones@mail.neversion.com>";
	const subject = getEmailSubject(subscription.serviceName, subscription.daysRemaining);
	const html = buildRenewalEmailHtml(subscription);

	try {
		const response = await fetch("https://api.resend.com/emails", {
			method: "POST",
			headers: {
				Authorization: `Bearer ${env.RESEND_API_KEY}`,
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				from: sender,
				to: [subscription.clientEmail],
				subject,
				html,
			}),
		});

		if (!response.ok) {
			const errorBody = await response.text();
			return {
				ok: false,
				error: `Error de Resend API (${response.status}): ${errorBody}`,
			};
		}

		const data = (await response.json()) as { id: string };
		return { ok: true, id: data.id };
	} catch (err: any) {
		return {
			ok: false,
			error: err.message || "Error inesperado al conectar con Resend",
		};
	}
}
