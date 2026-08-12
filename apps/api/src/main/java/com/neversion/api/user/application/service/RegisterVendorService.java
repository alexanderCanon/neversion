package com.neversion.api.user.application.service;

import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
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

/**
 * Application service implementing US-012 — Vendor Registration.
 * <p>
 * Flow (Backend-Driven Auth):
 * <ol>
 *   <li>Service creates the Auth account and explicitly injects the VENDOR role.</li>
 *   <li>Receives the Auth UUID as {@code externalId}.</li>
 *   <li>This service persists the internal User and Vendor records using that externalId.</li>
 *   <li>Records a VENDOR_WELCOME event in notification_log for Agent Notifications.</li>
 * </ol>
 * </p>
 */
@Service
public class RegisterVendorService implements RegisterVendorUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final AuthServicePort authServicePort;

    public RegisterVendorService(
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            NotificationLogPort notificationLogPort,
            AuthServicePort authServicePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.authServicePort = authServicePort;
    }

    @Override
    @Transactional
    public RegisterVendorResult register(RegisterVendorCommand command) {
        // Step 1 — Create Auth user securely and get the externalId
        String externalId = authServicePort.createUser(command.email(), command.password(), UserRole.VENDOR);

        // Step 2 — Persist the internal platform user with the Supabase-provided externalId
        User user = userRepositoryPort.save(
                User.builder()
                        .externalId(externalId)
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

        // Step 4 — Record welcome event for Agent Notifications (NFR-05)
        String payload = String.format(
                "{\"email\":\"%s\",\"storeName\":\"%s\",\"externalId\":\"%s\"}",
                command.email(), command.storeName(), externalId);
        notificationLogPort.record("VENDOR_WELCOME", command.email(), payload,
                "vendor", vendor.getId(), "welcome");

        return new RegisterVendorResult(
                user.getExternalId(),
                vendor.getUuid(),
                vendor.getStoreName(),
                command.email());
    }
}

