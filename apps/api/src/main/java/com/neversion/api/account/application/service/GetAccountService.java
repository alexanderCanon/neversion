package com.neversion.api.account.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.neversion.api.account.application.port.in.GetAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;

@Service
public class GetAccountService implements GetAccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;

    public GetAccountService(AccountRepositoryPort accountRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
    }

    @Override
    public Account getById(UUID uuid) {
        return accountRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + uuid));
    }

    @Override
    public List<Account> getByServiceId(Long serviceId) {
        return accountRepositoryPort.findByServiceId(serviceId);
    }

    @Override
    public List<Account> getAll() {
        return accountRepositoryPort.findAll();
    }
}
