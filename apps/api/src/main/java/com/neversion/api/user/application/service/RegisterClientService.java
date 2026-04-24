package com.neversion.api.user.application.service;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.in.RegisterClientUseCase;
import com.neversion.api.user.domain.model.RegisterClientCommand;
import com.neversion.api.user.domain.model.RegisterClientResult;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service implementing US-013 — Client Self-Registration.
 * <p>
 * Orchestrates the full client onboarding flow:
 * <ol>
 *   <li>Resolves the vendor by UUID (multi-tenancy: ADR-02).</li>
 *   <li>Generates a secure temporary password.</li>
 *   <li>Persists the internal User record (role=CLIENT).</li>
 *   <li>Persists the Client record linked to both user and vendor.</li>
 *   <li>Records a CLIENT_REGISTRATION event in notification_log
 *       so Agent Notifications can dispatch the confirmation email.</li>
 * </ol>
 *
 * <p><b>MANUAL STEP REQUIRED (ADR-09):</b> After registration, the
 * Supabase Auth account must be created manually. The externalId
 * stored is a placeholder that must be updated with the Supabase user UUID.
 */
@Service
public class RegisterClientService implements RegisterClientUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final NotificationLogPort notificationLogPort;

    public RegisterClientService(
            UserRepositoryPort userRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            NotificationLogPort notificationLogPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.notificationLogPort = notificationLogPort;
    }

    @Override
    @Transactional
    public RegisterClientResult register(RegisterClientCommand command) {
        // Step 1 — Resolve the vendor by public UUID (ADR-02 multi-tenancy)
        Vendor vendor = vendorRepositoryPort.findByUuid(command.vendorUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found: " + command.vendorUuid()));

        // BR-US013-01: Generate a secure temporary password for the client.
        String temporaryPassword = generateTemporaryPassword();

        // BR-US013-02: Placeholder external_id until Supabase Auth account is created.
        String placeholderExternalId = "pending_" + UUID.randomUUID();

        // Step 2 — Persist the internal platform user with role CLIENT
        User user = userRepositoryPort.save(
                User.builder()
                        .externalId(placeholderExternalId)
                        .role(UserRole.CLIENT)
                        .build());

        // Step 3 — Persist the client record linked to user and vendor
        Client client = clientRepositoryPort.save(
                Client.builder()
                        .userId(user.getId())
                        .vendorId(vendor.getId())
                        .name(command.name())
                        .email(command.email())
                        .phone(command.phone())
                        .build());

        // Step 4 — Record notification event for Agent Notifications (NFR-05)
        String payload = buildNotificationPayload(
                command.email(), command.name(), temporaryPassword, placeholderExternalId);
        notificationLogPort.record("CLIENT_REGISTRATION", command.email(), payload);

        return new RegisterClientResult(
                user.getUuid(),
                client.getUuid(),
                client.getName(),
                command.email(),
                temporaryPassword);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String buildNotificationPayload(
            String email, String name, String temporaryPassword, String placeholderExternalId) {
        return String.format(
                "{\"email\":\"%s\",\"name\":\"%s\",\"temporaryPassword\":\"%s\"," +
                "\"note\":\"Create Supabase Auth account manually with email=%s and this password. " +
                "Then update users.external_id where external_id='%s'.\"}",
                email, name, temporaryPassword, email, placeholderExternalId);
    }
}
