package com.neversion.api.profile.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.profile.domain.model.Profile;

public interface ProfileUseCase {

    Profile save(Profile profile);

    Optional<Profile> findById(UUID uuid);

    List<Profile> findByAccountId(Long accountId);

    List<Profile> findAvailableByAccountId(Long accountId);

    /** Auto-generates N blank profiles for an account upon creation (BR-01). */
    void generateProfilesForAccount(Long accountId, int count);

    void deleteById(UUID uuid);
}
