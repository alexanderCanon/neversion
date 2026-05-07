package com.neversion.api.user.application.service;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.SupabaseAuthPort;
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
 *   <li>Service creates the Supabase Auth account directly with the CLIENT role.</li>
 *   <li>Receives the Supabase UUID as {@code externalId}.</li>
 *   <li>This service persists the internal User and Client records using that externalId.</li>
 *   <li>Records a CLIENT_REGISTRATION event in notification_log for Agent Notifications.</li>
 * </ol>
 */
@Service
public class RegisterClientService implements RegisterClientUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final SupabaseAuthPort supabaseAuthPort;

    public RegisterClientService(
            UserRepositoryPort userRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            NotificationLogPort notificationLogPort,
            SupabaseAuthPort supabaseAuthPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.supabaseAuthPort = supabaseAuthPort;
    }

    @Override
    @Transactional
    public RegisterClientResult register(RegisterClientCommand command) {
        // Step 1 — Resolve the vendor by public UUID (ADR-02 multi-tenancy)
        Vendor vendor = vendorRepositoryPort.findByUuid(command.vendorUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found: " + command.vendorUuid()));

        // Step 2 — Create Supabase Auth user securely and get the externalId
        String externalId = supabaseAuthPort.createUser(command.email(), command.password(), UserRole.CLIENT);

        // Step 3 — Persist the internal platform user with the Supabase-provided externalId
        User user = userRepositoryPort.save(
                User.builder()
                        .externalId(externalId)
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

        // Step 5 — Record notification event for Agent Notifications (NFR-05)
        String payload = String.format(
                "{\"email\":\"%s\",\"name\":\"%s\",\"externalId\":\"%s\"}",
                command.email(), command.name(), externalId);
        notificationLogPort.record("CLIENT_REGISTRATION", command.email(), payload,
                "client", client.getId(), "welcome");

        return new RegisterClientResult(
                user.getUuid(),
                client.getUuid(),
                client.getName(),
                command.email());
    }
}
