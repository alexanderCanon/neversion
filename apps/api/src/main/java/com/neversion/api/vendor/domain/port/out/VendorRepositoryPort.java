package com.neversion.api.vendor.domain.port.out;

import com.neversion.api.vendor.domain.model.Vendor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — contract for vendor persistence.
 * Implemented by the JPA adapter in the infrastructure layer.
 */
public interface VendorRepositoryPort {

    Vendor save(Vendor vendor);

    Optional<Vendor> findByUuid(UUID uuid);

    /** US-033: Lookup by internal PK when only the Long id is available (e.g., from client.vendorId). */
    Optional<Vendor> findByInternalId(Long id);

    Optional<Vendor> findByUserId(Long userId);

    List<Vendor> findAll();

    boolean existsByUserId(Long userId);

    void deleteByUuid(UUID uuid);
}
