package com.neversion.api.client.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.client.application.port.in.ClientUseCase;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;

@Service
public class ClientService implements ClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    public ClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    @Transactional
    public Client create(Client client) {
        return clientRepositoryPort.save(client);
    }

    @Override
    @Transactional
    public Client update(UUID uuid, Client updated) {
        Client existing = clientRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + uuid));
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setNotes(updated.getNotes());
        return clientRepositoryPort.save(existing);
    }

    @Override
    public Client getById(UUID uuid) {
        return clientRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + uuid));
    }

    @Override
    public List<Client> getByName(String name) {
        return clientRepositoryPort.findByName(name);
    }

    @Override
    public List<Client> getByPhone(String phone) {
        return clientRepositoryPort.findByPhone(phone);
    }

    @Override
    public List<Client> getAll() {
        return clientRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        clientRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + uuid));
        clientRepositoryPort.deleteById(uuid);
    }
}
