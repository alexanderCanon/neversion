package com.neversion.api.user.domain.port.out;

import com.neversion.api.user.domain.model.User;

import java.util.Optional;

/**
 * Outbound port — contract for user persistence.
 * Implemented by the JPA adapter in the infrastructure layer.
 */
public interface UserRepositoryPort {

    User save(User user);

    /** US-034: Lookup by internal PK for cross-module notification resolution. */
    Optional<User> findById(Long id);

    Optional<User> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);
}

