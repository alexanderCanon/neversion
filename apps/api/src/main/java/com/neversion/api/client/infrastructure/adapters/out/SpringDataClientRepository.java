package com.neversion.api.client.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByUuid(UUID uuid);

    List<ClientEntity> findByName(String name);

    List<ClientEntity> findByPhone(String phone);
}
