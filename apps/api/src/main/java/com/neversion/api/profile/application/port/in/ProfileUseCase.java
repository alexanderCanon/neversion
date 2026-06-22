package com.neversion.api.profile.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;

public interface ProfileUseCase {

    Profile save(Profile profile);

    Optional<Profile> findById(UUID uuid);

    List<Profile> findByAccountId(Long accountId);

    List<Profile> findAvailableByAccountId(Long accountId);

    List<Profile> findByAccountUuid(UUID accountUuid);

    List<Profile> findAvailableByAccountUuid(UUID accountUuid);

    /**
     * Auto-generates N blank profiles upon account creation (BR-01).
     * vendorId propagated for multi-tenancy (ADR-02).
     */
    void generateProfilesForAccount(Long accountId, int count, Long vendorId);

    /**
     * US-025: Generates profiles on demand with ownership check and maxProfiles validation.
     * @param accountUuid  external UUID of the parent account
     * @param count        number of profiles to generate
     * @param callerExternalId  Supabase externalId from JWT
     */
    List<Profile> generate(UUID accountUuid, int count, String callerExternalId);

    /**
     * US-026: Edits a profile's mutable fields (name, pin, notes, isOwner) with ownership check.
     * @param profileUuid  external UUID of the profile
     * @param name         new name (null = no change)
     * @param pin          new pin  (null = no change)
     * @param notes        new notes (null = no change)
     * @param isOwner      new isOwner flag (null = no change)
     * @param callerExternalId  Supabase externalId from JWT
     */
    Profile update(UUID profileUuid, String name, String pin, String notes, Boolean isOwner, String callerExternalId);

    /**
     * US-027: Changes profile status manually. Only AVAILABLE ↔ BLOCKED allowed.
     * ACTIVE, RESERVED, OCCUPIED, EXPIRED are system-controlled — throws 400 if attempted.
     */
    Profile changeStatus(UUID profileUuid, ProfileStatus newStatus, String callerExternalId);

    void deleteById(UUID uuid);
}
