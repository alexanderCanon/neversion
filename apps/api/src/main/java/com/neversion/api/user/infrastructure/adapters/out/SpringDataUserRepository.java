package com.neversion.api.user.infrastructure.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository — infrastructure adapter.
 * Not exposed beyond the JpaUserAdapter.
 */
interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);
}

