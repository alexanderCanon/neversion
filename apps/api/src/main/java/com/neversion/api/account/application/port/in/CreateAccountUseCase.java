package com.neversion.api.account.application.port.in;

import com.neversion.api.account.domain.model.Account;

public interface CreateAccountUseCase {
    Account create(Account account);
}
