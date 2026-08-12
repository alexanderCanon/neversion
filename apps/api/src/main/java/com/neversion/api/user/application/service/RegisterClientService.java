package com.neversion.api.user.application.service;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
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

/**
 * Application service implementing US-013 — Client Self-Registration.
 * <p>
 * Flow (Backend-Driven Auth):
 * <ol>
 *   <li>Resolves the vendor by UUID (multi-tenancy: ADR-02).</li>
 *   <li>Links an existing manual client when the vendor-scoped phone matches.</li>
 *   <li>Service creates the Auth account directly with the CLIENT role.</li>
 *   <li>Receives the Auth UUID as {@code externalId}.</li>
 *   <li>This service persists the internal User and Client records using that externalId.</li>
 *   <li>Records a CLIENT_REGISTRATION event in notification_log for Agent Notifications.</li>
 *   </ol>
 * </p>
 */
@Service
public class RegisterClientService implements RegisterClientUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final AuthServicePort authServicePort;

    public RegisterClientService(
            UserRepositoryPort userRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            NotificationLogPort notificationLogPort,
            AuthServicePort authServicePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.authServicePort = authServicePort;
    }

    @Override
    @Transactional
    public RegisterClientResult register(RegisterClientCommand command) {
        // Idempotency: check if user already exists locally by externalId
        if (hasText(command.externalId())) {
            var existingUser = userRepositoryPort.findByExternalId(command.externalId());
            if (existingUser.isPresent()) {
                Client client = clientRepositoryPort.findByUserId(existingUser.get().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Client record missing for authenticated user: " + command.externalId()));

                return new RegisterClientResult(
                        existingUser.get().getExternalId(),
                        client.getUuid(),
                        client.getName(),
                        client.getEmail());

            }
        }

        // Step 1 — Resolve the vendor by public UUID (ADR-02 multi-tenancy)
        Vendor vendor = vendorRepositoryPort.findByUuid(command.vendorUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found: " + command.vendorUuid()));

        String normalizedPhone = normalizePhone(command.phone());
        if (normalizedPhone.isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
        String normalizedEmail = normalizeEmail(command.email());

        Client existingClient = clientRepositoryPort.findByVendorIdAndPhone(vendor.getId(), normalizedPhone)
                .orElse(null);
        if (existingClient != null && existingClient.getUserId() != null) {
            throw new IllegalArgumentException("Phone already linked to an authenticated client");
        }
        if (existingClient != null
                && hasText(existingClient.getEmail())
                && !existingClient.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Phone belongs to a client with a different email");
        }
        if (existingClient == null) {
            clientRepositoryPort.findByEmail(normalizedEmail).ifPresent(client -> {
                throw new IllegalArgumentException("Email already registered: " + normalizedEmail);
            });
        }

        // Step 2 — Create Auth user securely and get the externalId
        boolean oauthUser = hasText(command.externalId());
        String externalId = command.externalId();
        if (!oauthUser) {
            if (!hasText(command.password())) {
                throw new IllegalArgumentException("Password is required for standard registration");
            }
            externalId = authServicePort.createUser(normalizedEmail, command.password(), UserRole.CLIENT);
        }

        // Step 3 — Persist the internal platform user with the Supabase-provided externalId
        User user = userRepositoryPort.save(
                User.builder()
                        .externalId(externalId)
                        .role(UserRole.CLIENT)
                        .build());

        // Step 4 — For OAuth users, stamp role=CLIENT in Supabase app_metadata.
        // Standard users already have the role set during createUser().
        if (oauthUser) {
            authServicePort.updateAppMetadata(externalId, UserRole.CLIENT);
        }

        Client client;
        if (existingClient != null) {
            existingClient.setUserId(user.getId());
            existingClient.setName(command.name());
            existingClient.setEmail(normalizedEmail);
            existingClient.setPhone(normalizedPhone);
            client = clientRepositoryPort.save(existingClient);
        } else {
            client = clientRepositoryPort.save(
                    Client.builder()
                            .userId(user.getId())
                            .vendorId(vendor.getId())
                            .name(command.name())
                            .email(normalizedEmail)
                            .phone(normalizedPhone)
                            .build());
        }

        // Step 5 — Record notification event for Agent Notifications (NFR-05)
        String payload = String.format(
                "{\"email\":\"%s\",\"name\":\"%s\",\"externalId\":\"%s\"}",
                normalizedEmail, command.name(), externalId);
        notificationLogPort.record("CLIENT_REGISTRATION", normalizedEmail, payload,
                "client", client.getId(), "welcome");

        return new RegisterClientResult(
                user.getExternalId(),
                client.getUuid(),
                client.getName(),
                normalizedEmail);

    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeEmail(String email) {
        return hasText(email) ? email.trim().toLowerCase() : null;
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }
}
