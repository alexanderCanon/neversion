package com.neversion.api.account.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.application.port.in.CreateAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-022: Vendor registers a master streaming account.
 * vendorId is resolved from the JWT caller — not accepted from the request body (ADR-09).
 *
 * Auto-generates N profiles if saleMode = BY_PROFILE (BR-01, US-025).
 * FULL_ACCOUNT accounts get one owner profile used as the subscription anchor.
 */
@Service
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ProfileUseCase profileUseCase;

    public CreateAccountService(AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ProfileUseCase profileUseCase) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.profileUseCase = profileUseCase;
    }

    @Override
    @Transactional
    public Account create(Account account, String callerExternalId) {
        // BR-US022-01: renewal date required
        if (account.getRenewalDate() == null) {
            throw new BusinessRuleException("Renewal date is required");
        }

        // Resolve vendorId from JWT caller (ADR-09)
        Long vendorId = resolveVendorId(callerExternalId);

        // Resolve service UUID → internal Long (frontend sends UUID only)
        com.neversion.api.service.domain.model.Service service =
                serviceRepositoryPort.findById(account.getServiceUuid())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Service not found: " + account.getServiceUuid()));

        // Resolve maxProfiles: use request value if provided, else inherit from service template
        int resolvedMaxProfiles = account.getMaxProfiles() != null && account.getMaxProfiles() > 0
                ? account.getMaxProfiles()
                : (service.getMaxProfiles() != null ? service.getMaxProfiles() : 1);

        Account toSave = Account.builder()
                .email(account.getEmail())
                .password(account.getPassword())
                .serviceId(service.getId())          // resolved internal Long
                .saleMode(account.getSaleMode())
                .renewalDate(account.getRenewalDate())
                .plan(account.getPlan())
                .cost(account.getCost())
                .source(account.getSource())
                .purchasedAt(account.getPurchasedAt())
                .notes(account.getNotes())
                .maxProfiles(resolvedMaxProfiles)
                .vendorId(vendorId)
                .build();

        Account saved = accountRepositoryPort.save(toSave);

        // BR-01: auto-generate profiles only for BY_PROFILE sale mode (US-025)
        if (account.getSaleMode() != null) {
            switch (account.getSaleMode()) {
                case BY_PROFILE -> {
                    int count = saved.getMaxProfiles();
                    profileUseCase.generateProfilesForAccount(saved.getId(), count, vendorId);
                }
                case FULL_ACCOUNT -> profileUseCase.generateProfilesForAccount(saved.getId(), 1, vendorId);
            }
        }

        return saved;
    }

    // ─── Ownership helper ────────────────────────────────────────────────────

    /** Resolves Supabase externalId → User → Vendor internal id. */
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
