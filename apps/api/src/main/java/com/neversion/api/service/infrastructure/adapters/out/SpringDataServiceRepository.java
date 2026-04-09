package com.neversion.api.service.infrastructure.adapters.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataServiceRepository extends JpaRepository<ServiceEntity, Long> {

    Optional<ServiceEntity> findByUuid(UUID uuid);

    Optional<ServiceEntity> findByName(String name);

    boolean existsByName(String name);
}
