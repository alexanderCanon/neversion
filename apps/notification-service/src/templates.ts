interface TemplateSpec {
  subject: string;
  htmlBody: string;
}

const getLayout = (title: string, headerTitle: string, gradient: string, bodyContent: string): string => {
  return `<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>${title}</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <!-- Header -->
                <tr>
                    <td style="background:${gradient};padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">${headerTitle}</h1>
                    </td>
                </tr>
                <!-- Body -->
                <tr>
                    <td style="padding:32px 40px;">
                        ${bodyContent}
                    </td>
                </tr>
                <!-- Footer -->
                <tr>
                    <td style="padding:24px 40px;background-color:#f9fafb;text-align:center;border-top:1px solid #e5e7eb;">
                        <p style="margin:0;color:#9ca3af;font-size:12px;">
                            © 2026 Neversion. Todos los derechos reservados.
                        </p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>`;
};

export const resolveTemplate = (eventType: string, payloadStr: string): TemplateSpec => {
  let payload: any = {};
  if (payloadStr) {
    try {
      payload = JSON.parse(payloadStr);
    } catch (e) {
      console.warn(`Failed to parse template payload JSON: ${payloadStr}`);
      payload = { rawPayload: payloadStr };
    }
  }

  let subject = 'Notificación de Neversion';
  let title = 'Notificación';
  let headerTitle = 'Notificación';
  let gradient = 'linear-gradient(135deg,#6366f1,#8b5cf6)'; // default Indigo
  let bodyContent = '';

  const clientName = payload.clientName || 'Cliente';
  const vendorName = payload.vendorName || 'Vendedor';
  const serviceName = payload.serviceName || '';
  const total = payload.total;
  const reason = payload.reason;
  const accountEmail = payload.accountEmail || '';
  const accountPassword = payload.accountPassword || '';
  const profileName = payload.profileName || '';
  const profilePin = payload.profilePin || '';
  const paymentDueDate = payload.paymentDueDate || '';
  const daysRemaining = payload.daysRemaining;
  const storeName = payload.storeName || 'vendedor';
  const renewalDate = payload.renewalDate || '';

  switch (eventType) {
    case 'CLIENT_WELCOME':
    case 'CLIENT_REGISTRATION':
      subject = '¡Bienvenido a Neversion!';
      title = 'Bienvenido a Neversion';
      headerTitle = '¡Bienvenido!';
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu registro en <strong>Neversion</strong> ha sido exitoso.
            Ahora puedes acceder a nuestros servicios digitales.
        </p>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Si tienes alguna pregunta, no dudes en contactarnos.
        </p>
      `;
      break;

    case 'VENDOR_WELCOME':
      subject = '¡Bienvenido a Neversion, Vendedor!';
      title = 'Bienvenido a Neversion, Vendedor';
      headerTitle = '¡Bienvenido, Vendedor!';
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${vendorName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu cuenta de vendedor en <strong>Neversion</strong> ha sido creada con éxito.
            Ya puedes acceder al panel de administración y comenzar a gestionar tus clientes y servicios.
        </p>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Si necesitas soporte, ponte en contacto con el administrador de la plataforma.
        </p>
      `;
      break;

    case 'PAYMENT_APPROVED':
      subject = 'Tu pago ha sido aprobado';
      title = 'Pago Aprobado';
      headerTitle = '¡Pago Aprobado!';
      gradient = 'linear-gradient(135deg,#059669,#10b981)'; // Green
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu pago ha sido verificado y aprobado exitosamente.
            Pronto recibirás los accesos a tu servicio.
        </p>
        ${total ? `
        <div style="background-color:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#166534;font-size:14px;">
                <strong>Total:</strong> Q<span>${total}</span>
            </p>
        </div>
        ` : ''}
      `;
      break;

    case 'RECEIPT_UPLOADED':
      subject = 'Comprobante recibido';
      title = 'Comprobante Recibido';
      headerTitle = 'Comprobante Recibido';
      gradient = 'linear-gradient(135deg,#2563eb,#3b82f6)'; // Blue
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hemos recibido tu comprobante de pago. Está en proceso de revisión por parte del equipo de soporte.
            Te notificaremos tan pronto como sea validado.
        </p>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Gracias por tu confianza.
        </p>
      `;
      break;

    case 'RECEIPT_REJECTED':
      subject = 'Comprobante rechazado';
      title = 'Comprobante Rechazado';
      headerTitle = 'Comprobante Rechazado';
      gradient = 'linear-gradient(135deg,#dc2626,#ef4444)'; // Red
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Lamentablemente tu comprobante de pago ha sido rechazado.
        </p>
        ${reason ? `
        <div style="background-color:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#991b1b;font-size:14px;">
                <strong>Motivo:</strong> <span>${reason}</span>
            </p>
        </div>
        ` : ''}
        <p style="margin:16px 0 0;color:#374151;font-size:16px;line-height:1.6;">
            Por favor, sube un comprobante válido desde tu panel para reanudar el procesamiento de tu orden.
        </p>
      `;
      break;

    case 'ACCESS_DELIVERED':
      subject = 'Tus accesos están listos';
      title = 'Tus Accesos';
      headerTitle = '¡Tus accesos están listos!';
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            A continuación encontrarás los datos de acceso a tu servicio:
        </p>
        <div style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:16px 0;">
            <table role="presentation" width="100%" cellspacing="0" cellpadding="4">
                ${serviceName ? `<tr><td style="color:#6b7280;font-size:13px;width:120px;">Servicio:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${serviceName}</td></tr>` : ''}
                ${accountEmail ? `<tr><td style="color:#6b7280;font-size:13px;">Correo:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${accountEmail}</td></tr>` : ''}
                ${accountPassword ? `<tr><td style="color:#6b7280;font-size:13px;">Contraseña:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${accountPassword}</td></tr>` : ''}
                ${profileName ? `<tr><td style="color:#6b7280;font-size:13px;">Perfil:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${profileName}</td></tr>` : ''}
                ${profilePin ? `<tr><td style="color:#6b7280;font-size:13px;">PIN:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${profilePin}</td></tr>` : ''}
                ${paymentDueDate ? `<tr><td style="color:#6b7280;font-size:13px;">Vence:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${paymentDueDate}</td></tr>` : ''}
            </table>
        </div>
        <p style="margin:16px 0 0;color:#ef4444;font-size:13px;font-style:italic;">
            ⚠️ No compartas estos datos con terceros. Son exclusivos para tu uso personal.
        </p>
      `;
      break;

    case 'ACCESS_REVOKED':
      subject = 'Acceso revocado';
      title = 'Acceso Revocado';
      headerTitle = 'Acceso Revocado';
      gradient = 'linear-gradient(135deg,#dc2626,#ef4444)'; // Red
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu acceso al servicio <strong>${serviceName}</strong> ha sido revocado.
        </p>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Si consideras que esto es un error o deseas renovar tu suscripción, ponte en contacto con nosotros.
        </p>
      `;
      break;

    case 'SUBSCRIPTION_RENEWED':
      subject = 'Renovación confirmada';
      title = 'Renovación Confirmada';
      headerTitle = '¡Renovación Confirmada!';
      gradient = 'linear-gradient(135deg,#059669,#10b981)'; // Green
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu renovación ha sido confirmada con éxito.
        </p>
        <div style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:16px 0;">
            <table role="presentation" width="100%" cellspacing="0" cellpadding="4">
                ${serviceName ? `<tr><td style="color:#6b7280;font-size:13px;width:120px;">Servicio:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${serviceName}</td></tr>` : ''}
                <tr><td style="color:#6b7280;font-size:13px;">Nueva fecha de vencimiento:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">${paymentDueDate || 'N/A'}</td></tr>
            </table>
        </div>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            ¡Gracias por continuar con nosotros!
        </p>
      `;
      break;

    case 'SUBSCRIPTION_EXPIRED':
      subject = 'Tu suscripción ha vencido';
      title = 'Tu suscripción ha vencido';
      headerTitle = 'Suscripción Vencida';
      gradient = 'linear-gradient(135deg,#dc2626,#ef4444)'; // Red
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu suscripción a <strong>${serviceName || 'tu servicio'}</strong> ha vencido y tus accesos han sido suspendidos.
        </p>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Puedes reactivar tu servicio realizando un nuevo pago y subiendo el comprobante en la plataforma.
        </p>
      `;
      break;

    case 'RENEWAL_REMINDER_7D':
      subject = 'Tu suscripción vence en 7 días';
      title = '¡Próximo Vencimiento!';
      headerTitle = '¡Próximo Vencimiento!';
      gradient = 'linear-gradient(135deg,#f59e0b,#d97706)'; // Orange
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu suscripción a <strong>${serviceName || 'tu servicio'}</strong> vence el <strong>${paymentDueDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#92400e;font-size:14px;">
                <strong>${daysRemaining !== undefined ? daysRemaining + ' días restantes' : 'Próximo a vencer'}</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Te sugerimos renovar con anticipación para no perder el acceso.
        </p>
      `;
      break;

    case 'RENEWAL_REMINDER_3D':
      subject = 'Tu suscripción vence en 3 días';
      title = '¡Próximo Vencimiento!';
      headerTitle = '¡Próximo Vencimiento!';
      gradient = 'linear-gradient(135deg,#f59e0b,#d97706)'; // Orange
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu suscripción a <strong>${serviceName || 'tu servicio'}</strong> vence el <strong>${paymentDueDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#92400e;font-size:14px;">
                <strong>${daysRemaining !== undefined ? daysRemaining + ' días restantes' : 'Próximo a vencer'}</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Te sugerimos renovar con anticipación para no perder el acceso.
        </p>
      `;
      break;

    case 'RENEWAL_REMINDER_1D':
      subject = 'Tu suscripción vence mañana';
      title = '¡Vence mañana!';
      headerTitle = '¡Vence mañana!';
      gradient = 'linear-gradient(135deg,#f59e0b,#d97706)'; // Orange
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Tu suscripción a <strong>${serviceName || 'tu servicio'}</strong> vence el <strong>${paymentDueDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#92400e;font-size:14px;">
                <strong>${daysRemaining !== undefined ? daysRemaining + ' días restantes' : 'Próximo a vencer'}</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
            Te sugerimos renovar con anticipación para no perder el acceso.
        </p>
      `;
      break;

    case 'ACCOUNT_RENEWAL_REMINDER_7D':
      subject = 'Renovación de cuenta matriz en 7 días';
      title = 'Renovación Cuenta Matriz';
      headerTitle = 'Renovación Cuenta Matriz';
      gradient = 'linear-gradient(135deg,#2563eb,#0f766e)'; // teal/blue
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${storeName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            La cuenta matriz de <strong>${serviceName || 'tu servicio'}</strong> debe renovarse el <strong>${renewalDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#eff6ff;border:1px solid #bfdbfe;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#1e40af;font-size:14px;">
                <strong>${daysRemaining === 0 ? 'La renovación es hoy' : daysRemaining + ' días restantes'}</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#374151;font-size:14px;">
            Cuenta: <strong>${accountEmail || 'Sin correo registrado'}</strong>
        </p>
      `;
      break;

    case 'ACCOUNT_RENEWAL_REMINDER_3D':
      subject = 'Renovación de cuenta matriz en 3 días';
      title = 'Renovación Cuenta Matriz';
      headerTitle = 'Renovación Cuenta Matriz';
      gradient = 'linear-gradient(135deg,#2563eb,#0f766e)'; // teal/blue
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${storeName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            La cuenta matriz de <strong>${serviceName || 'tu servicio'}</strong> debe renovarse el <strong>${renewalDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#eff6ff;border:1px solid #bfdbfe;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#1e40af;font-size:14px;">
                <strong>${daysRemaining === 0 ? 'La renovación es hoy' : daysRemaining + ' días restantes'}</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#374151;font-size:14px;">
            Cuenta: <strong>${accountEmail || 'Sin correo registrado'}</strong>
        </p>
      `;
      break;

    case 'ACCOUNT_RENEWAL_REMINDER_1D':
      subject = 'Renovación de cuenta matriz mañana';
      title = 'Renovación Cuenta Matriz';
      headerTitle = 'Renovación Cuenta Matriz';
      gradient = 'linear-gradient(135deg,#2563eb,#0f766e)'; // teal/blue
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${storeName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            La cuenta matriz de <strong>${serviceName || 'tu servicio'}</strong> debe renovarse el <strong>${renewalDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#eff6ff;border:1px solid #bfdbfe;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#1e40af;font-size:14px;">
                <strong>${daysRemaining === 0 ? 'La renovación es hoy' : daysRemaining + ' días restantes'}</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#374151;font-size:14px;">
            Cuenta: <strong>${accountEmail || 'Sin correo registrado'}</strong>
        </p>
      `;
      break;

    case 'ACCOUNT_RENEWAL_REMINDER_DUE':
      subject = 'Renovación de cuenta matriz hoy';
      title = 'Renovación Cuenta Matriz';
      headerTitle = 'Renovación Cuenta Matriz';
      gradient = 'linear-gradient(135deg,#2563eb,#0f766e)'; // teal/blue
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${storeName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            La cuenta matriz de <strong>${serviceName || 'tu servicio'}</strong> debe renovarse el <strong>${renewalDate || 'próximamente'}</strong>.
        </p>
        <div style="background-color:#eff6ff;border:1px solid #bfdbfe;border-radius:6px;padding:16px;margin:16px 0;">
            <p style="margin:0;color:#1e40af;font-size:14px;">
                <strong>La renovación es hoy</strong>
            </p>
        </div>
        <p style="margin:0 0 8px;color:#374151;font-size:14px;">
            Cuenta: <strong>${accountEmail || 'Sin correo registrado'}</strong>
        </p>
      `;
      break;

    case 'ORDER_COMPLETED':
      subject = 'Tu orden ha sido completada';
      title = 'Orden Completada';
      headerTitle = 'Orden Completada';
      gradient = 'linear-gradient(135deg,#059669,#10b981)'; // Green
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            ¡Tu orden ha sido procesada y completada satisfactoriamente!
            Muchas gracias por tu compra.
        </p>
      `;
      break;

    case 'ORDER_CANCELLED':
      subject = 'Tu orden ha sido cancelada';
      title = 'Orden Cancelada';
      headerTitle = 'Orden Cancelada';
      gradient = 'linear-gradient(135deg,#6b7280,#9ca3af)'; // Gray
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Hola <strong>${clientName}</strong>,
        </p>
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            Te informamos que tu orden ha sido cancelada.
        </p>
      `;
      break;

    default:
      // Fallback generic
      bodyContent = `
        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
            ${payload.rawPayload || 'Tienes una nueva notificación de Neversion.'}
        </p>
      `;
      break;
  }

  const htmlBody = getLayout(title, headerTitle, gradient, bodyContent);
  return { subject, htmlBody };
};
