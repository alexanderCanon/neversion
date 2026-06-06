package com.neversion.api.notification.application.service.template;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * EPIC-08: Resolves notification type → Thymeleaf template + subject line.
 * Parses the JSON payload and feeds variables into the template engine.
 */
@Component
public class NotificationTemplateResolver {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateResolver.class);

    /** Internal-only notification types that should NOT trigger an email. */
    private static final Set<String> INTERNAL_TYPES = Set.of(
            "NO_INVENTORY_ALERT",
            "SUBSCRIPTIONS_EXPIRED_DAILY"
    );

    private static final Map<String, TemplateMapping> TEMPLATES = Map.ofEntries(
            Map.entry("CLIENT_WELCOME", new TemplateMapping("email/client-welcome", "¡Bienvenido a Neversion!")),
            Map.entry("CLIENT_REGISTRATION", new TemplateMapping("email/client-welcome", "¡Bienvenido a Neversion!")),
            Map.entry("VENDOR_WELCOME", new TemplateMapping("email/vendor-welcome", "¡Bienvenido a Neversion, Vendedor!")),
            Map.entry("PAYMENT_APPROVED", new TemplateMapping("email/payment-approved", "Tu pago ha sido aprobado")),
            Map.entry("RECEIPT_UPLOADED", new TemplateMapping("email/receipt-uploaded", "Comprobante recibido")),
            Map.entry("RECEIPT_REJECTED", new TemplateMapping("email/receipt-rejected", "Comprobante rechazado")),
            Map.entry("ACCESS_DELIVERED", new TemplateMapping("email/access-delivered", "Tus accesos están listos")),
            Map.entry("ACCESS_REVOKED", new TemplateMapping("email/access-revoked", "Acceso revocado")),
            Map.entry("SUBSCRIPTION_RENEWED", new TemplateMapping("email/subscription-renewed", "Renovación confirmada")),
            Map.entry("SUBSCRIPTION_EXPIRED", new TemplateMapping("email/subscription-expired", "Tu suscripción ha vencido")),
            Map.entry("RENEWAL_REMINDER_7D", new TemplateMapping("email/renewal-reminder", "Tu suscripción vence en 7 días")),
            Map.entry("RENEWAL_REMINDER_3D", new TemplateMapping("email/renewal-reminder", "Tu suscripción vence en 3 días")),
            Map.entry("RENEWAL_REMINDER_1D", new TemplateMapping("email/renewal-reminder", "Tu suscripción vence mañana")),
            Map.entry("ACCOUNT_RENEWAL_REMINDER_7D", new TemplateMapping("email/account-renewal-reminder", "Renovación de cuenta matriz en 7 días")),
            Map.entry("ACCOUNT_RENEWAL_REMINDER_3D", new TemplateMapping("email/account-renewal-reminder", "Renovación de cuenta matriz en 3 días")),
            Map.entry("ACCOUNT_RENEWAL_REMINDER_1D", new TemplateMapping("email/account-renewal-reminder", "Renovación de cuenta matriz mañana")),
            Map.entry("ACCOUNT_RENEWAL_REMINDER_DUE", new TemplateMapping("email/account-renewal-reminder", "Renovación de cuenta matriz hoy")),
            Map.entry("ORDER_COMPLETED", new TemplateMapping("email/order-completed", "Tu orden ha sido completada")),
            Map.entry("ORDER_CANCELLED", new TemplateMapping("email/order-cancelled", "Tu orden ha sido cancelada"))
    );

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public NotificationTemplateResolver(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Resolves a notification type + JSON payload into a renderable email.
     *
     * @param type    notification type string
     * @param payload JSON string with template variables
     * @return TemplateSpec with subject + rendered HTML, or skip=true for internal types
     */
    public TemplateSpec resolve(String type, String payload) {
        if (INTERNAL_TYPES.contains(type)) {
            return TemplateSpec.skip();
        }

        TemplateMapping mapping = TEMPLATES.get(type);
        if (mapping == null) {
            log.warn("Unknown notification type '{}', using fallback template", type);
            mapping = new TemplateMapping("email/generic", "Notificación de Neversion");
        }

        Map<String, Object> variables = parsePayload(payload);

        Context context = new Context();
        context.setVariables(variables);
        context.setVariable("notificationType", type);

        String htmlBody = templateEngine.process(mapping.templateName(), context);
        return new TemplateSpec(mapping.subject(), htmlBody, false);
    }

    private Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse notification payload as JSON: {}", e.getMessage());
            return Map.of("rawPayload", payload);
        }
    }

    // ── Inner types ──────────────────────────────────────────────────────────

    private record TemplateMapping(String templateName, String subject) {}

    /**
     * Resolved template specification ready for sending.
     */
    public record TemplateSpec(String subject, String htmlBody, boolean skipped) {
        public static TemplateSpec skip() {
            return new TemplateSpec(null, null, true);
        }
    }
}
