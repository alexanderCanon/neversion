package com.neversion.api.client.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataClientRepository
        extends JpaRepository<ClientEntity, Long>, JpaSpecificationExecutor<ClientEntity> {

    Optional<ClientEntity> findByUuid(UUID uuid);

    Optional<ClientEntity> findByEmail(String email);

    Optional<ClientEntity> findByUserId(Long userId);

    List<ClientEntity> findByVendorId(Long vendorId);

    List<ClientEntity> findByName(String name);

    List<ClientEntity> findByPhone(String phone);
}
