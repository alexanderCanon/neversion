package com.neversion.api.account.application.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.application.port.in.UpdateAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-023: Vendor updates a master account they own.
 * Only the owner vendor can edit; 403 thrown otherwise.
 * id and uuid are never mutated (BR-US023-01).
 */
@Service
public class UpdateAccountService implements UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public UpdateAccountService(AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional
    public Account update(UUID uuid, Account updates, String callerExternalId) {
        Account existing = accountRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + uuid));

        // Ownership check (BR-US023-01)
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(existing.getVendorId())) {
            throw new AccessDeniedException("Access denied: you do not own account " + uuid);
        }

        // saleMode change: allowed only if no active subscriptions on any profile
        if (updates.getSaleMode() != null && existing.getSaleMode() != null
                && !updates.getSaleMode().equals(existing.getSaleMode())) {
            var profiles = profileRepositoryPort.findByAccountId(existing.getId());
            boolean hasActiveSubscriptions = profiles.stream()
                    .anyMatch(p -> subscriptionRepositoryPort.existsActiveByProfileId(p.getId()));
            if (hasActiveSubscriptions) {
                throw new BusinessRuleException(
                        "No se puede cambiar la modalidad de venta de una cuenta que tiene suscripciones activas.");
            }
        }

        // maxProfiles change: cannot reduce below existing profile count
        if (updates.getMaxProfiles() != null && updates.getMaxProfiles() > 0) {
            int currentProfileCount = profileRepositoryPort.findByAccountId(existing.getId()).size();
            if (updates.getMaxProfiles() < currentProfileCount) {
                throw new BusinessRuleException(
                        "No se puede reducir maxProfiles por debajo del número de perfiles existentes ("
                                + currentProfileCount + ").");
            }
        }

        // Resolve service UUID → internal Long if provided
        Long resolvedServiceId = existing.getServiceId();
        if (updates.getServiceUuid() != null) {
            var service = serviceRepositoryPort.findById(updates.getServiceUuid())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Service not found: " + updates.getServiceUuid()));
            resolvedServiceId = service.getId();
        }
        // Apply all editable fields — id, uuid, vendorId are immutable
        existing.setEmail(updates.getEmail());
        existing.setPassword(updates.getPassword());
        existing.setServiceId(resolvedServiceId);
        existing.setRenewalDate(updates.getRenewalDate());
        existing.setPlan(updates.getPlan());
        existing.setCost(updates.getCost());
        existing.setSource(updates.getSource());
        existing.setPurchasedAt(updates.getPurchasedAt());
        existing.setNotes(updates.getNotes());
        if (updates.getSaleMode() != null) {
            existing.setSaleMode(updates.getSaleMode());
        }
        if (updates.getMaxProfiles() != null && updates.getMaxProfiles() > 0) {
            existing.setMaxProfiles(updates.getMaxProfiles());
        }

        return accountRepositoryPort.save(existing);
    }

    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor record not found for user: " + user.getExternalId()))
                .getId();
    }
}
