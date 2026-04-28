package com.neversion.api.account.application.port.in;

import com.neversion.api.account.domain.model.Account;

public interface CreateAccountUseCase {
    /** Creates a master account. vendorId resolved from JWT caller (ADR-09). */
    Account create(Account account, String callerExternalId);
}
