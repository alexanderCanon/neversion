package com.neversion.api.account.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.account.domain.model.Account;

public interface GetAccountUseCase {
    Account getById(UUID uuid);
    List<Account> getByServiceId(Long serviceId);
    List<Account> getAll();
}
