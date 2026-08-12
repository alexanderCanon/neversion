package com.neversion.api.account.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

public interface ListAccountsUseCase {
    /**
     * US-024: Returns all accounts for a vendor with optional filters.
     * @param vendorUuid  external UUID of the vendor
     * @param serviceUuid optional filter by service UUID
     * @param status      optional filter by account status
     */
    List<Account> listByVendor(UUID vendorUuid, UUID serviceUuid, AccountStatus status, String callerExternalId);

    List<Account> listAccounts(UUID serviceUuid, AccountStatus status, String callerExternalId);
}

