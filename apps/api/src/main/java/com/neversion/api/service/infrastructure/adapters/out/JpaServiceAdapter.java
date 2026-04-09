package com.neversion.api.service.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.service.infrastructure.adapters.out.mapper.ServicePersistenceMapper;

@Repository
public class JpaServiceAdapter implements ServiceRepositoryPort {

    private final SpringDataServiceRepository serviceRepo;
    private final ServicePersistenceMapper serviceMapper;

    public JpaServiceAdapter(SpringDataServiceRepository serviceRepo,
            ServicePersistenceMapper serviceMapper) {
        this.serviceRepo = serviceRepo;
        this.serviceMapper = serviceMapper;
    }

    @Override
    public Service save(Service service) {
        ServiceEntity entity = serviceMapper.toEntity(service);
        ServiceEntity saved = serviceRepo.saveAndFlush(entity);
        return serviceMapper.toDomain(saved);
    }

    @Override
    public Optional<Service> findById(UUID uuid) {
        return serviceRepo.findByUuid(uuid).map(serviceMapper::toDomain);
    }

    @Override
    public Optional<Service> findByInternalId(Long id) {
        return serviceRepo.findById(id).map(serviceMapper::toDomain);
    }

    @Override
    public Optional<Service> findByName(String name) {
        return serviceRepo.findByName(name).map(serviceMapper::toDomain);
    }

    @Override
    public List<Service> findAll() {
        return serviceRepo.findAll().stream()
                .map(serviceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return serviceRepo.existsByName(name);
    }

    @Override
    public void deleteById(UUID uuid) {
        serviceRepo.findByUuid(uuid).ifPresent(e -> serviceRepo.deleteById(e.getId()));
    }
}
