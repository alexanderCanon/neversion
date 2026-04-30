package com.neversion.api.shared.infrastructure.adapters.out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.neversion.api.shared.port.out.EmailSenderPort;

/**
 * EPIC-08: Resend API adapter for sending transactional emails.
 * Implements the EmailSenderPort hexagonal boundary.
 */
@Component
public class ResendEmailAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailAdapter.class);

    private final Resend resend;
    private final String fromAddress;

    public ResendEmailAdapter(
            @Value("${neversion.email.resend.api-key}") String apiKey,
            @Value("${neversion.email.from:Neversion <noreply@neversion.com>}") String fromAddress) {
        this.resend = new Resend(apiKey);
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String to, String subject, String htmlBody) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(to)
                .subject(subject)
                .html(htmlBody)
                .build();

        try {
            var response = resend.emails().send(params);
            log.info("Email sent via Resend to={}, subject='{}', resendId={}",
                    to, subject, response.getId());
        } catch (ResendException e) {
            log.error("Failed to send email via Resend to={}, subject='{}'", to, subject, e);
            throw new EmailSendException("Resend API error: " + e.getMessage(), e);
        }
    }
}
