package com.neversion.api.profile.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.profile.domain.model.Profile;

public interface ProfileRepositoryPort {

    Profile save(Profile profile);

    Optional<Profile> findById(UUID uuid);

    Optional<Profile> findByInternalId(Long id);

    List<Profile> findByAccountId(Long accountId);

    /** Returns profiles not yet linked to any active subscription (BR-04). */
    List<Profile> findAvailableByAccountId(Long accountId);

    /**
     * US-033: Counts profiles with status AVAILABLE for a given service
     * across all accounts of the specified vendor. Used to validate
     * profile availability before creating a reservation.
     */
    long countAvailableByServiceIdAndVendorId(Long serviceId, Long vendorId);

    /**
     * Returns profiles with status AVAILABLE for a given service across all
     * accounts of the specified vendor, ordered by creation date ASC.
     * Used by batch subscription creation for auto-assignment.
     */
    List<Profile> findAvailableByServiceIdAndVendorId(Long serviceId, Long vendorId);

    void saveAll(List<Profile> profiles);

    void deleteById(UUID uuid);
}
