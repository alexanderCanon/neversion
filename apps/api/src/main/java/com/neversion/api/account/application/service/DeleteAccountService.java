package com.neversion.api.account.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.application.port.in.DeleteAccountUseCase;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;

/**
 * Hard-deletes an account by UUID. Cascades to profiles via DB ON DELETE CASCADE.
 * Only valid if no active subscriptions reference profiles of this account
 * (enforced at DB level via FK constraint).
 */
@Service
public class DeleteAccountService implements DeleteAccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;

    public DeleteAccountService(AccountRepositoryPort accountRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        accountRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + uuid));
        accountRepositoryPort.deleteById(uuid);
    }
}
