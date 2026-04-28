package com.neversion.api.client.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.client.domain.model.Client;

public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(UUID uuid);

    Optional<Client> findByInternalId(Long id);

    /** US-029 — Buscar dentro del tenant del vendor con filtros opcionales. */
    List<Client> findByVendorId(Long vendorId, String name, String phone, String email);

    /** US-031 — Validación de unicidad de email antes de persistir. */
    Optional<Client> findByEmail(String email);

    List<Client> findByName(String name);

    List<Client> findByPhone(String phone);

    List<Client> findAll();

    void deleteById(UUID uuid);
}
