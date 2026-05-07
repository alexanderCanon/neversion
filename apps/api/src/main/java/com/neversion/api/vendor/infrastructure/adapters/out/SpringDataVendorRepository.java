package com.neversion.api.vendor.infrastructure.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository — package-private, not exposed beyond JpaVendorAdapter.
 */
interface SpringDataVendorRepository extends JpaRepository<VendorEntity, Long> {

    Optional<VendorEntity> findByUuid(UUID uuid);

    Optional<VendorEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUuid(UUID uuid);
}
