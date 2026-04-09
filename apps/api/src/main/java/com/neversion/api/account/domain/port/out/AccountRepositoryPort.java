package com.neversion.api.account.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.account.domain.model.Account;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(UUID uuid);

    Optional<Account> findByInternalId(Long id);

    List<Account> findByServiceId(Long serviceId);

    List<Account> findAll();

    void deleteById(UUID uuid);
}
