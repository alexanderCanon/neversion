package com.neversion.api.service.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.service.domain.model.Service;

public interface ServiceUseCase {
    Service create(Service service);
    Service update(UUID uuid, Service service);
    Service getById(UUID uuid);
    List<Service> getAll();
    void delete(UUID uuid);
}
