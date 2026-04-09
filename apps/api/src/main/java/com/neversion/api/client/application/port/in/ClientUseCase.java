package com.neversion.api.client.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.client.domain.model.Client;

public interface ClientUseCase {
    Client create(Client client);
    Client update(UUID uuid, Client client);
    Client getById(UUID uuid);
    List<Client> getByName(String name);
    List<Client> getByPhone(String phone);
    List<Client> getAll();
    void delete(UUID uuid);
}
