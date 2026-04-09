package com.neversion.api.account.application.port.in;

import java.util.UUID;

public interface DeleteAccountUseCase {
    void delete(UUID uuid);
}
