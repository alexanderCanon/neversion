# Guía de Plantillas de Email para Resend (Neversion)

Esta guía contiene la lista completa de las **19 plantillas HTML** listas para copiar y pegar directamente en el panel de **Resend** (sección **Templates** -> **Create Template**).

---

## 1. `client-welcome` (Bienvenida Cliente)
- **Slug / ID en Resend**: `client-welcome`
- **Asunto por defecto**: `¡Bienvenido a Neversion!`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Bienvenido a Neversion</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">¡Bienvenido!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu registro en <strong>Neversion</strong> ha sido exitoso.
                            Ahora puedes acceder a nuestros servicios digitales.
                        </p>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            Si tienes alguna pregunta, no dudes en contactarnos.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 2. `vendor-welcome` (Bienvenida Vendedor)
- **Slug / ID en Resend**: `vendor-welcome`
- **Asunto por defecto**: `¡Bienvenido a Neversion, Vendedor!`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Bienvenido Vendedor</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">¡Bienvenido, Vendedor!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ vendorName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu cuenta de vendedor en <strong>Neversion</strong> ha sido creada con éxito.
                            Ya puedes acceder al panel de administración y comenzar a gestionar tus clientes y servicios.
                        </p>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            Si necesitas soporte, ponte en contacto con el administrador de la plataforma.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 3. `payment-approved` (Pago Aprobado)
- **Slug / ID en Resend**: `payment-approved`
- **Asunto por defecto**: `Tu pago ha sido aprobado`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Pago Aprobado</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#059669,#10b981);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">¡Pago Aprobado!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu pago ha sido verificado y aprobado exitosamente.
                            Pronto recibirás los accesos a tu servicio.
                        </p>
                        {{#total}}
                        <div style="background-color:#f0fdf4;border:1px solid #bbf7d0;border-radius:6px;padding:16px;margin:16px 0;">
                            <p style="margin:0;color:#166534;font-size:14px;">
                                <strong>Total:</strong> Q<span>{{{ total }}}</span>
                            </p>
                        </div>
                        {{/total}}
                    </td>
                </tr>
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
</html>
```

---

## 4. `receipt-uploaded` (Comprobante Recibido - Cliente)
- **Slug / ID en Resend**: `receipt-uploaded`
- **Asunto por defecto**: `Comprobante recibido`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Comprobante Recibido</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#2563eb,#3b82f6);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Comprobante Recibido</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hemos recibido tu comprobante de pago. Está en proceso de revisión por parte del equipo de soporte.
                            Te notificaremos tan pronto como sea validado.
                        </p>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            Gracias por tu confianza.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 5. `vendor-receipt-uploaded` (Nuevo Comprobante - Vendedor)
- **Slug / ID en Resend**: `vendor-receipt-uploaded`
- **Asunto por defecto**: `Nuevo comprobante de pago recibido`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Nuevo Comprobante</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#2563eb,#3b82f6);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Nuevo Comprobante</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ storeName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Un cliente ha subido un comprobante de pago para la reserva con ID <strong>{{{ reservationId }}}</strong> por un total de <strong>Q{{{ total }}}</strong>.
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Por favor, ingresa a tu panel de administración para validar y procesar este pago.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 6. `receipt-rejected` (Comprobante Rechazado)
- **Slug / ID en Resend**: `receipt-rejected`
- **Asunto por defecto**: `Comprobante rechazado`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Comprobante Rechazado</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#dc2626,#ef4444);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Comprobante Rechazado</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Lamentablemente tu comprobante de pago ha sido rechazado.
                        </p>
                        {{#reason}}
                        <div style="background-color:#fef2f2;border:1px solid #fecaca;border-radius:6px;padding:16px;margin:16px 0;">
                            <p style="margin:0;color:#991b1b;font-size:14px;">
                                <strong>Motivo:</strong> <span>{{{ reason }}}</span>
                            </p>
                        </div>
                        {{/reason}}
                        <p style="margin:16px 0 0;color:#374151;font-size:16px;line-height:1.6;">
                            Por favor, sube un comprobante válido desde tu panel para reanudar el procesamiento de tu orden.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 7. `access-delivered` (Entrega de Accesos)
- **Slug / ID en Resend**: `access-delivered`
- **Asunto por defecto**: `Tus accesos están listos`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Tus Accesos</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">¡Tus accesos están listos!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            A continuación encontrarás los datos de acceso a tu servicio:
                        </p>
                        <div style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:16px 0;">
                            <table role="presentation" width="100%" cellspacing="0" cellpadding="4">
                                {{#serviceName}}<tr><td style="color:#6b7280;font-size:13px;width:120px;">Servicio:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ serviceName }}}</td></tr>{{/serviceName}}
                                {{#accountEmail}}<tr><td style="color:#6b7280;font-size:13px;">Correo:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ accountEmail }}}</td></tr>{{/accountEmail}}
                                {{#accountPassword}}<tr><td style="color:#6b7280;font-size:13px;">Contraseña:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ accountPassword }}}</td></tr>{{/accountPassword}}
                                {{#profileName}}<tr><td style="color:#6b7280;font-size:13px;">Perfil / Invitación:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ profileName }}}</td></tr>{{/profileName}}
                                {{#profilePin}}<tr><td style="color:#6b7280;font-size:13px;">PIN / Clave:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ profilePin }}}</td></tr>{{/profilePin}}
                                {{#paymentDueDate}}<tr><td style="color:#6b7280;font-size:13px;">Vence:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ paymentDueDate }}}</td></tr>{{/paymentDueDate}}
                            </table>
                        </div>
                        <p style="margin:16px 0 0;color:#ef4444;font-size:13px;font-style:italic;">
                            ⚠️ No compartas estos datos con terceros. Son exclusivos para tu uso personal.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 8. `access-revoked` (Acceso Revocado)
- **Slug / ID en Resend**: `access-revoked`
- **Asunto por defecto**: `Acceso revocado`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Acceso Revocado</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#dc2626,#ef4444);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Acceso Revocado</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu acceso al servicio <strong>{{{ serviceName }}}</strong> ha sido revocado.
                        </p>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            Si consideras que esto es un error o deseas renovar tu suscripción, ponte en contacto con nosotros.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 9. `subscription-renewed` (Renovación Confirmada)
- **Slug / ID en Resend**: `subscription-renewed`
- **Asunto por defecto**: `Renovación confirmada`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Renovación Confirmada</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#059669,#10b981);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">¡Renovación Confirmada!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu renovación ha sido confirmada con éxito.
                        </p>
                        <div style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:16px 0;">
                            <table role="presentation" width="100%" cellspacing="0" cellpadding="4">
                                {{#serviceName}}<tr><td style="color:#6b7280;font-size:13px;width:120px;">Servicio:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ serviceName }}}</td></tr>{{/serviceName}}
                                <tr><td style="color:#6b7280;font-size:13px;">Nueva fecha de vencimiento:</td><td style="color:#1f2937;font-size:14px;font-weight:600;">{{{ paymentDueDate }}}</td></tr>
                            </table>
                        </div>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            ¡Gracias por continuar con nosotros!
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 10. `subscription-expired` (Suscripción Vencida)
- **Slug / ID en Resend**: `subscription-expired`
- **Asunto por defecto**: `Tu suscripción ha vencido`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Suscripción Vencida</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#dc2626,#ef4444);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Suscripción Vencida</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu suscripción a <strong>{{{ serviceName }}}</strong> ha vencido y tus accesos han sido suspendidos.
                        </p>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            Puedes reactivar tu servicio realizando un nuevo pago y subiendo el comprobante en la plataforma.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 11-13. Recordatorios de Renovación (7 días, 3 días y 1 día)
- **Slugs / IDs en Resend**: `renewal-reminder-7d`, `renewal-reminder-3d`, `renewal-reminder-1d`
- **Asuntos**:
  - `renewal-reminder-7d`: `Tu suscripción vence en 7 días`
  - `renewal-reminder-3d`: `Tu suscripción vence en 3 días`
  - `renewal-reminder-1d`: `Tu suscripción vence mañana`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Próximo Vencimiento</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#f59e0b,#d97706);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">¡Próximo Vencimiento!</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Tu suscripción a <strong>{{{ serviceName }}}</strong> vence el <strong>{{{ paymentDueDate }}}</strong>.
                        </p>
                        <div style="background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:16px;margin:16px 0;">
                            <p style="margin:0;color:#92400e;font-size:14px;">
                                <strong>{{{ daysRemaining }}} días restantes</strong>
                            </p>
                        </div>
                        <p style="margin:0 0 8px;color:#6b7280;font-size:14px;">
                            Te sugerimos renovar con anticipación para no perder el acceso.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 14-17. Recordatorios Cuenta Matriz Vendedor
- **Slugs / IDs en Resend**: `account-renewal-reminder-7d`, `account-renewal-reminder-3d`, `account-renewal-reminder-1d`, `account-renewal-reminder-due`
- **Asuntos**: `Renovación de cuenta matriz en X días / hoy`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Renovación Cuenta Matriz</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#2563eb,#0f766e);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Renovación Cuenta Matriz</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ storeName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            La cuenta matriz de <strong>{{{ serviceName }}}</strong> debe renovarse el <strong>{{{ renewalDate }}}</strong>.
                        </p>
                        <div style="background-color:#eff6ff;border:1px solid #bfdbfe;border-radius:6px;padding:16px;margin:16px 0;">
                            <p style="margin:0;color:#1e40af;font-size:14px;">
                                <strong>{{{ daysRemaining }}} días restantes</strong>
                            </p>
                        </div>
                        <p style="margin:0 0 8px;color:#374151;font-size:14px;">
                            Cuenta: <strong>{{{ accountEmail }}}</strong>
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 18. `order-completed` (Orden Completada)
- **Slug / ID en Resend**: `order-completed`
- **Asunto por defecto**: `Tu orden ha sido completada`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Orden Completada</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#059669,#10b981);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Orden Completada</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            ¡Tu orden ha sido procesada y completada satisfactoriamente!
                            Muchas gracias por tu compra.
                        </p>
                    </td>
                </tr>
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
</html>
```

---

## 19. `order-cancelled` (Orden Cancelada)
- **Slug / ID en Resend**: `order-cancelled`
- **Asunto por defecto**: `Tu orden ha sido cancelada`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Orden Cancelada</title>
</head>
<body style="margin:0;padding:0;font-family:'Segoe UI',Roboto,Arial,sans-serif;background-color:#f4f4f7;">
<table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f7;padding:40px 0;">
    <tr>
        <td align="center">
            <table role="presentation" width="560" cellspacing="0" cellpadding="0"
                   style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                <tr>
                    <td style="background:linear-gradient(135deg,#6b7280,#9ca3af);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">Orden Cancelada</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding:32px 40px;">
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Hola <strong>{{{ clientName }}}</strong>,
                        </p>
                        <p style="margin:0 0 16px;color:#374151;font-size:16px;line-height:1.6;">
                            Te informamos que tu orden ha sido cancelada.
                        </p>
                    </td>
                </tr>
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
</html>
```
