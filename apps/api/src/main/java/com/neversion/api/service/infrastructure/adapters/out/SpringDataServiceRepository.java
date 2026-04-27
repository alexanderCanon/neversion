package com.neversion.api.service.infrastructure.adapters.out;

import com.neversion.api.shared.domain.model.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataServiceRepository extends JpaRepository<ServiceEntity, Long> {

    Optional<ServiceEntity> findByUuid(UUID uuid);

    Optional<ServiceEntity> findByName(String name);

    boolean existsByName(String name);

    /** All services (active and inactive) for a vendor. US-020. */
    List<ServiceEntity> findAllByVendorId(Long vendorId);

    /** Active services for a vendor — public store catalog. US-021. */
    List<ServiceEntity> findAllByVendorIdAndIsActiveTrue(Long vendorId);

    /**
     * Filtered query for the vendor panel. Category and isActive are optional.
     * US-020: filter by category and/or status.
     */
    @Query("""
            SELECT s FROM ServiceEntity s
            WHERE s.vendorId = :vendorId
              AND (:category IS NULL OR s.category = :category)
              AND (:isActive IS NULL OR s.isActive = :isActive)
            """)
    List<ServiceEntity> findByVendorIdAndFilters(
            @Param("vendorId") Long vendorId,
            @Param("category") CategoryType category,
            @Param("isActive") Boolean isActive);
}
