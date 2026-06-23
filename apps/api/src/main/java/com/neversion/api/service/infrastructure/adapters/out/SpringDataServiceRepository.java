package com.neversion.api.service.infrastructure.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataServiceRepository
        extends JpaRepository<ServiceEntity, Long>, JpaSpecificationExecutor<ServiceEntity> {

    Optional<ServiceEntity> findByUuid(UUID uuid);

    Optional<ServiceEntity> findByName(String name);

    boolean existsByName(String name);

    /** All services (active and inactive) for a vendor. US-020. */
    List<ServiceEntity> findAllByVendorId(Long vendorId);

    /** Active services for a vendor — public store catalog. US-021. */
    List<ServiceEntity> findAllByVendorIdAndIsActiveTrue(Long vendorId);

}
