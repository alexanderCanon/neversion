package com.neversion.api.account.application.port.in;

import java.util.UUID;

import com.neversion.api.account.domain.model.Account;

public interface UpdateAccountUseCase {
    /**
     * US-023: Updates editable fields of an account.
     * id and uuid are immutable and will never be changed.
     * callerExternalId used to verify ownership (403 if not owner).
     */
    Account update(UUID uuid, Account updates, String callerExternalId);
}
