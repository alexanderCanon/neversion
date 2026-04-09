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

    void saveAll(List<Profile> profiles);

    void deleteById(UUID uuid);
}
