package com.neversion.api.account.domain.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(UUID uuid);

    Optional<Account> findByInternalId(Long id);

    List<Account> findByServiceId(Long serviceId);

    List<Account> findByServiceIdAndVendorId(Long serviceId, Long vendorId);

    List<Account> findByRenewalDate(LocalDate renewalDate);

    /** US-024: All accounts for a vendor. */
    List<Account> findByVendorId(Long vendorId);

    /**
     * US-024: Accounts for a vendor with optional filters.
     * Pass null to skip a filter.
     */
    List<Account> findByVendorIdFiltered(Long vendorId, Long serviceId, AccountStatus status);

    List<Account> findAll();

    void deleteById(UUID uuid);
}
