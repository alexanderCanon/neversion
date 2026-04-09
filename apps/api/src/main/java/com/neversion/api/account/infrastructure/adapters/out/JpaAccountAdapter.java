package com.neversion.api.account.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.account.infrastructure.adapters.out.mapper.AccountPersistenceMapper;

@Repository
public class JpaAccountAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository accountRepo;
    private final AccountPersistenceMapper accountMapper;

    public JpaAccountAdapter(SpringDataAccountRepository accountRepo,
            AccountPersistenceMapper accountMapper) {
        this.accountRepo = accountRepo;
        this.accountMapper = accountMapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity saved = accountRepo.saveAndFlush(entity);
        return accountMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID uuid) {
        return accountRepo.findByUuid(uuid).map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByInternalId(Long id) {
        return accountRepo.findById(id).map(accountMapper::toDomain);
    }

    @Override
    public List<Account> findByServiceId(Long serviceId) {
        return accountRepo.findByServiceId(serviceId).stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findAll() {
        return accountRepo.findAll().stream()
                .map(accountMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID uuid) {
        accountRepo.findByUuid(uuid).ifPresent(e -> accountRepo.deleteById(e.getId()));
    }
}
