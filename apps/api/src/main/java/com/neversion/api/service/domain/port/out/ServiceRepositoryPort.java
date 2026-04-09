package com.neversion.api.service.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.service.domain.model.Service;

public interface ServiceRepositoryPort {

    Service save(Service service);

    Optional<Service> findById(UUID uuid);

    Optional<Service> findByInternalId(Long id);

    Optional<Service> findByName(String name);

    List<Service> findAll();

    boolean existsByName(String name);

    void deleteById(UUID uuid);
}
