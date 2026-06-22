package com.neversion.api.account.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataAccountRepository
        extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByUuid(UUID uuid);

    List<AccountEntity> findByServiceId(Long serviceId);

    List<AccountEntity> findByServiceIdAndVendorId(Long serviceId, Long vendorId);

    List<AccountEntity> findByRenewalDate(LocalDate renewalDate);

    /** US-024: All accounts for a vendor. */
    List<AccountEntity> findByVendorId(Long vendorId);
}
