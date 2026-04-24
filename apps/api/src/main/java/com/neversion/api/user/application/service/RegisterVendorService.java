package com.neversion.api.user.application.service;

import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.in.RegisterVendorUseCase;
import com.neversion.api.user.domain.model.RegisterVendorCommand;
import com.neversion.api.user.domain.model.RegisterVendorResult;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service implementing US-012 — Vendor Registration.
 * <p>
 * Orchestrates the full vendor onboarding flow:
 * <ol>
 *   <li>Generates a secure temporary password.</li>
 *   <li>Persists the internal User record (role=VENDOR).</li>
 *   <li>Persists the Vendor record linked to the user.</li>
 *   <li>Records a VENDOR_WELCOME event in notification_log
 *       so Agent Notifications can dispatch the onboarding email.</li>
 * </ol>
 *
 * <p><b>MANUAL STEP REQUIRED (ADR-09):</b> After calling this endpoint,
 * the Super Admin must create the corresponding Supabase Auth account
 * using the Supabase dashboard or Admin API with the returned
 * temporaryPassword. The externalId stored in the User record is a
 * placeholder UUID that must be updated once the Supabase user is created.
 */
@Service
public class RegisterVendorService implements RegisterVendorUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final NotificationLogPort notificationLogPort;

    public RegisterVendorService(
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            NotificationLogPort notificationLogPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.notificationLogPort = notificationLogPort;
    }

    @Override
    @Transactional
    public RegisterVendorResult register(RegisterVendorCommand command) {
        // BR-US012-01: Generate a secure temporary password for the vendor.
        // This password is sent via notification_log and must be used when
        // manually creating the Supabase Auth account (ADR-09).
        String temporaryPassword = generateTemporaryPassword();

        // BR-US012-02: A placeholder external_id is used until the Super Admin
        // creates the Supabase Auth account and updates the externalId.
        String placeholderExternalId = "pending_" + UUID.randomUUID();

        // Step 1 — Persist the internal platform user with role VENDOR
        User user = userRepositoryPort.save(
                User.builder()
                        .externalId(placeholderExternalId)
                        .role(UserRole.VENDOR)
                        .build());

        // Step 2 — Persist the vendor record linked to the user
        Vendor vendor = vendorRepositoryPort.save(
                Vendor.builder()
                        .userId(user.getId())
                        .storeName(command.storeName())
                        .logoUrl(command.logoUrl())
                        .bankDetails(command.bankDetails())
                        .discountCfg(command.discountCfg())
                        .build());

        // Step 3 — Record notification event for Agent Notifications (NFR-05)
        String payload = buildNotificationPayload(
                command.email(), command.storeName(), temporaryPassword, placeholderExternalId);
        notificationLogPort.record("VENDOR_WELCOME", command.email(), payload);

        return new RegisterVendorResult(
                user.getUuid(),
                vendor.getUuid(),
                vendor.getStoreName(),
                command.email(),
                temporaryPassword);
    }

    /**
     * Generates a URL-safe temporary password (12 characters).
     * Derived from a random UUID — sufficient entropy for a one-time credential.
     */
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * Builds the JSON payload stored in notification_log.
     * Agent Notifications uses these fields to render the welcome email template.
     */
    private String buildNotificationPayload(
            String email, String storeName, String temporaryPassword, String placeholderExternalId) {
        return String.format(
                "{\"email\":\"%s\",\"storeName\":\"%s\",\"temporaryPassword\":\"%s\"," +
                "\"note\":\"Create Supabase Auth account manually with email=%s and this password. " +
                "Then update users.external_id where external_id='%s'.\"}",
                email, storeName, temporaryPassword, email, placeholderExternalId);
    }
}
