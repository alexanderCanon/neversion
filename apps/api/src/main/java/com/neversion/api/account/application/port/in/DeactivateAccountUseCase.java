package com.neversion.api.account.application.port.in;

import java.util.UUID;

public interface DeactivateAccountUseCase {
    void deactivate(UUID id);
}
