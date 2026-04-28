package com.neversion.api.account.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountDetailResponse;

public interface GetAccountUseCase {
    Account getById(UUID uuid);
    List<Account> getByServiceId(Long serviceId);
    List<Account> getAll();

    /** US-028: Returns the account with all profiles and summary counters. */
    AccountDetailResponse getDetail(UUID uuid, String callerExternalId);
}
