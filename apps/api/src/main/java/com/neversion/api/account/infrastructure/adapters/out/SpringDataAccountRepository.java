package com.neversion.api.account.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neversion.api.shared.domain.model.enums.AccountStatus;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByUuid(UUID uuid);

    List<AccountEntity> findByServiceId(Long serviceId);

    /** US-024: All accounts for a vendor. */
    List<AccountEntity> findByVendorId(Long vendorId);

    /**
     * US-024: Accounts for a vendor with optional service and status filters.
     * Uses JPQL coalesce-style: null param = skip that filter.
     */
    @Query("""
            SELECT a FROM AccountEntity a
            WHERE a.vendorId = :vendorId
              AND (:serviceId IS NULL OR a.serviceId = :serviceId)
              AND (:status IS NULL OR a.status = :status)
            ORDER BY a.createdAt DESC
            """)
    List<AccountEntity> findByVendorIdFiltered(
            @Param("vendorId") Long vendorId,
            @Param("serviceId") Long serviceId,
            @Param("status") AccountStatus status);
}
