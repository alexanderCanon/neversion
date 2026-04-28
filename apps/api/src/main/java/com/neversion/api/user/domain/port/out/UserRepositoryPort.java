package com.neversion.api.user.domain.port.out;

import com.neversion.api.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — contract for user persistence.
 * Implemented by the JPA adapter in the infrastructure layer.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    void deleteByUuid(UUID uuid);
}
