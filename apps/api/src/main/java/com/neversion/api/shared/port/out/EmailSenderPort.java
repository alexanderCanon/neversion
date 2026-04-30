package com.neversion.api.shared.port.out;

/**
 * EPIC-08: Outbound port for sending emails.
 * Hexagonal boundary — services never touch the email provider directly.
 */
public interface EmailSenderPort {

    /**
     * Sends an email.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param htmlBody HTML content body
     * @throws EmailSendException if sending fails
     */
    void send(String to, String subject, String htmlBody);

    /**
     * Exception wrapper for email sending failures.
     */
    class EmailSendException extends RuntimeException {
        public EmailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
