package com.neversion.api.service.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.application.port.in.ServiceUseCase;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;

/**
 * CRUD operations for the Service catalog (BR-17: name must be unique).
 * 'details' JSONB is passed through as-is — no domain processing required.
 */
@Service
public class DigitalServiceService implements ServiceUseCase {

    private final ServiceRepositoryPort serviceRepositoryPort;

    public DigitalServiceService(ServiceRepositoryPort serviceRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
    }

    @Override
    @Transactional
    public com.neversion.api.service.domain.model.Service create(
            com.neversion.api.service.domain.model.Service service) {
        if (serviceRepositoryPort.existsByName(service.getName())) {
            throw new BusinessRuleException(
                    "A service named '" + service.getName() + "' already exists.");
        }
        return serviceRepositoryPort.save(service);
    }

    @Override
    @Transactional
    public com.neversion.api.service.domain.model.Service update(UUID uuid,
            com.neversion.api.service.domain.model.Service updated) {
        com.neversion.api.service.domain.model.Service existing =
                serviceRepositoryPort.findById(uuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Service not found: " + uuid));

        existing.setName(updated.getName());
        existing.setMaxProfiles(updated.getMaxProfiles());
        existing.setDetails(updated.getDetails());
        return serviceRepositoryPort.save(existing);
    }

    @Override
    public com.neversion.api.service.domain.model.Service getById(UUID uuid) {
        return serviceRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + uuid));
    }

    @Override
    public List<com.neversion.api.service.domain.model.Service> getAll() {
        return serviceRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        serviceRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + uuid));
        serviceRepositoryPort.deleteById(uuid);
    }
}
