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

/**
 * Application service implementing US-012 — Vendor Registration.
 * <p>
 * Flow (ADR-09 revised — frontend-managed auth):
 * <ol>
 *   <li>Frontend creates the Supabase Auth account and lets the user choose their password.</li>
 *   <li>Frontend sends the resulting Supabase UUID as {@code externalId} in the request.</li>
 *   <li>This service persists the internal User and Vendor records using that externalId.</li>
 *   <li>Records a VENDOR_WELCOME event in notification_log for Agent Notifications.</li>
 * </ol>
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
        // Step 1 — Persist the internal platform user with the Supabase-provided externalId
        User user = userRepositoryPort.save(
                User.builder()
                        .externalId(command.externalId())
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

        // Step 3 — Record welcome event for Agent Notifications (NFR-05)
        String payload = String.format(
                "{\"email\":\"%s\",\"storeName\":\"%s\",\"externalId\":\"%s\"}",
                command.email(), command.storeName(), command.externalId());
        notificationLogPort.record("VENDOR_WELCOME", command.email(), payload);

        return new RegisterVendorResult(
                user.getUuid(),
                vendor.getUuid(),
                vendor.getStoreName(),
                command.email());
    }
}
