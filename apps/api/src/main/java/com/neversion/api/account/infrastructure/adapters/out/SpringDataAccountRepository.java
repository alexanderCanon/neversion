package com.neversion.api.account.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.neversion.api.shared.domain.model.enums.AccountStatus;

public interface SpringDataAccountRepository
        extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByUuid(UUID uuid);

    List<AccountEntity> findByServiceId(Long serviceId);

    List<AccountEntity> findByServiceIdAndVendorId(Long serviceId, Long vendorId);

    /** US-024: All accounts for a vendor. */
    List<AccountEntity> findByVendorId(Long vendorId);
}
