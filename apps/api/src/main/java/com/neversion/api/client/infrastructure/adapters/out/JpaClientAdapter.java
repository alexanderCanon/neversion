package com.neversion.api.client.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.client.infrastructure.adapters.out.mapper.ClientPersistenceMapper;

@Repository
public class JpaClientAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository clientRepo;
    private final ClientPersistenceMapper clientMapper;

    public JpaClientAdapter(SpringDataClientRepository clientRepo,
            ClientPersistenceMapper clientMapper) {
        this.clientRepo = clientRepo;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = clientMapper.toEntity(client);
        ClientEntity saved = clientRepo.saveAndFlush(entity);
        return clientMapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(UUID uuid) {
        return clientRepo.findByUuid(uuid).map(clientMapper::toDomain);
    }

    @Override
    public Optional<Client> findByInternalId(Long id) {
        return clientRepo.findById(id).map(clientMapper::toDomain);
    }

    @Override
    public List<Client> findByName(String name) {
        return clientRepo.findByName(name).stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    @Override
    public List<Client> findByPhone(String phone) {
        return clientRepo.findByPhone(phone).stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    @Override
    public List<Client> findAll() {
        return clientRepo.findAll().stream()
                .map(clientMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID uuid) {
        clientRepo.findByUuid(uuid).ifPresent(e -> clientRepo.deleteById(e.getId()));
    }
}
