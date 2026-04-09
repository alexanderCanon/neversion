package com.neversion.api.account.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByUuid(UUID uuid);

    List<AccountEntity> findByServiceId(Long serviceId);
}
