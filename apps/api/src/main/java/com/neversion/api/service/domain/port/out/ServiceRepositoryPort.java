package com.neversion.api.service.domain.port.out;

import com.neversion.api.service.domain.model.Service;
import com.neversion.api.shared.domain.model.enums.CategoryType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — contract for service catalog persistence.
 * Implemented by JpaServiceAdapter in the infrastructure layer.
 */
public interface ServiceRepositoryPort {

    Service save(Service service);

    Optional<Service> findById(UUID uuid);

    Optional<Service> findByInternalId(Long id);

    Optional<Service> findByName(String name);

    /** Returns ALL services belonging to a vendor (active and inactive). US-020. */
    List<Service> findAllByVendorId(Long vendorId);

    /**
     * Returns vendor services filtered by optional category and/or status. US-020.
     * Null parameters are treated as "no filter applied".
     */
    List<Service> findByVendorIdAndFilters(Long vendorId, CategoryType category, Boolean isActive);

    /** Returns only active services for a vendor. US-021 (public store catalog). */
    List<Service> findActiveByVendorId(Long vendorId);

    /** Returns all services across all vendors (super_admin). */
    List<Service> findAll();

    boolean existsByName(String name);

    void deleteById(UUID uuid);
}
