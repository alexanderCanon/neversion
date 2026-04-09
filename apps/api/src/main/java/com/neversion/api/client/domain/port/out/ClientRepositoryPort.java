package com.neversion.api.client.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.client.domain.model.Client;

public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(UUID uuid);

    Optional<Client> findByInternalId(Long id);

    List<Client> findByName(String name);

    List<Client> findByPhone(String phone);

    List<Client> findAll();

    void deleteById(UUID uuid);
}
