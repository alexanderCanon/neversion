package com.neversion.api.profile.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * EPIC-03 application service for profile management.
 *
 * US-025: generate profiles on demand with maxProfiles validation.
 * US-026: update mutable fields with ownership check.
 * US-027: change status manually — only AVAILABLE ↔ BLOCKED allowed.
 */
@Service
public class ProfileService implements ProfileUseCase {

    /** States that can only be set by business logic (EPIC-06 / EPIC-07). */
    private static final Set<ProfileStatus> SYSTEM_CONTROLLED =
            Set.of(ProfileStatus.ACTIVE, ProfileStatus.RESERVED,
                   ProfileStatus.OCCUPIED, ProfileStatus.EXPIRED);

    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public ProfileService(ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    // ─── Basic CRUD ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Profile save(Profile profile) {
        // Resolve accountUuid → accountId when coming from REST request (US-022 / US-026)
        if (profile.getAccountId() == null && profile.getAccountUuid() != null) {
            var account = accountRepositoryPort.findById(profile.getAccountUuid())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account not found: " + profile.getAccountUuid()));
            profile.setAccountId(account.getId());
        }
        return profileRepositoryPort.save(profile);
    }

    @Override
    public Optional<Profile> findById(UUID uuid) {
        return profileRepositoryPort.findById(uuid);
    }

    @Override
    public List<Profile> findByAccountId(Long accountId) {
        return profileRepositoryPort.findByAccountId(accountId);
    }

    @Override
    public List<Profile> findAvailableByAccountId(Long accountId) {
        return profileRepositoryPort.findAvailableByAccountId(accountId);
    }

    @Override
    public List<Profile> findByAccountUuid(UUID accountUuid) {
        var account = accountRepositoryPort.findById(accountUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountUuid));
        return profileRepositoryPort.findByAccountId(account.getId());
    }

    @Override
    public List<Profile> findAvailableByAccountUuid(UUID accountUuid) {
        var account = accountRepositoryPort.findById(accountUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountUuid));
        return profileRepositoryPort.findAvailableByAccountId(account.getId());
    }

    // ─── US-022 / BR-01: auto-generation on account creation ─────────────────

    /**
     * Called internally by CreateAccountService (BR-01).
     * Profile names default to "Perfil N" — vendor can rename later (US-026).
     */
    @Override
    @Transactional
    public void generateProfilesForAccount(Long accountId, int count, Long vendorId) {
        List<Profile> profiles = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            profiles.add(Profile.builder()
                    .accountId(accountId)
                    .name("Perfil " + i)
                    .isOwner(i == 1)
                    .vendorId(vendorId)
                    .build());
        }
        profileRepositoryPort.saveAll(profiles);
    }

    // ─── US-025: generate on demand ──────────────────────────────────────────

    @Override
    @Transactional
    public List<Profile> generate(UUID accountUuid, int count, String callerExternalId) {
        var account = accountRepositoryPort.findById(accountUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountUuid));

        // Ownership check (BR-US025-01)
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(account.getVendorId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access denied: you do not own account " + accountUuid);
        }

        if (SaleMode.FULL_ACCOUNT.equals(account.getSaleMode())) {
            throw new BusinessRuleException(
                    "Cannot generate profiles for a full-account sale mode account.");
        }

        // Validate maxProfiles against account-level limit (BR-US025-02)
        int maxProfiles = account.getMaxProfiles() != null ? account.getMaxProfiles() : 0;
        int existing = profileRepositoryPort.findByAccountId(account.getId()).size();
        if (maxProfiles > 0 && existing + count > maxProfiles) {
            throw new BusinessRuleException(
                    "Cannot generate " + count + " profiles: would exceed maxProfiles (" + maxProfiles + ") for this account. Current: " + existing);
        }

        List<Profile> profiles = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            profiles.add(Profile.builder()
                    .accountId(account.getId())
                    .name("Perfil " + (existing + i))
                    .isOwner(false)
                    .vendorId(callerVendorId)
                    .build());
        }
        profileRepositoryPort.saveAll(profiles);

        // Return full updated list for this account
        return profileRepositoryPort.findByAccountId(account.getId());
    }

    // ─── US-026: update mutable fields ───────────────────────────────────────

    @Override
    @Transactional
    public Profile update(UUID profileUuid, String name, String pin, String notes, Boolean isOwner,
            String callerExternalId) {
        Profile profile = profileRepositoryPort.findById(profileUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileUuid));

        assertProfileOwnership(profile, callerExternalId);

        // Apply only non-null values (BR-US026-01)
        if (name != null) profile.setName(name);
        if (pin != null) profile.setPin(pin);
        if (notes != null) profile.setNotes(notes);
        if (isOwner != null) profile.setIsOwner(isOwner);

        return profileRepositoryPort.save(profile);
    }

    // ─── US-027: change status manually ─────────────────────────────────────

    @Override
    @Transactional
    public Profile changeStatus(UUID profileUuid, ProfileStatus newStatus, String callerExternalId) {
        // BR-US027-01: only AVAILABLE and BLOCKED are allowed manually
        if (SYSTEM_CONTROLLED.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Status '" + newStatus.name().toLowerCase() + "' cannot be set manually. " +
                    "Allowed: available, blocked.");
        }

        Profile profile = profileRepositoryPort.findById(profileUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileUuid));

        assertProfileOwnership(profile, callerExternalId);

        profile.setStatus(newStatus);
        return profileRepositoryPort.save(profile);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteById(UUID uuid) {
        profileRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + uuid));
        profileRepositoryPort.deleteById(uuid);
    }

    // ─── Ownership helpers ───────────────────────────────────────────────────

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

    /**
     * Asserts the caller owns the account the profile belongs to (BR-US026-01 / BR-US027-01).
     * Resolves: profile → account → vendorId, compared to caller's vendorId.
     */
    private void assertProfileOwnership(Profile profile, String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);
        var account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for profile: " + profile.getUuid()));
        if (!callerVendorId.equals(account.getVendorId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access denied: you do not own the account for profile " + profile.getUuid());
        }
    }
}
