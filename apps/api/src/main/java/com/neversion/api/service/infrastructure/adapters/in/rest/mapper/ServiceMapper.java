package com.neversion.api.service.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceRequest;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceResponse;

@Component
public class ServiceMapper {

    private final ObjectMapper objectMapper;

    public ServiceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Service toDomain(ServiceRequest request) {
        if (request == null) return null;

        JsonNode detailsNode = null;
        if (request.details() != null && !request.details().isBlank()) {
            try {
                detailsNode = objectMapper.readTree(request.details());
            } catch (Exception e) {
                throw new IllegalArgumentException("'details' field contains invalid JSON: " + e.getMessage());
            }
        }

        return Service.builder()
                .name(request.name())
                .maxProfiles(request.maxProfiles())
                .details(detailsNode)
                .build();
    }

    public ServiceResponse toResponse(Service service) {
        if (service == null) return null;

        String detailsStr = null;
        if (service.getDetails() != null) {
            detailsStr = service.getDetails().toString();
        }

        return ServiceResponse.builder()
                .id(service.getUuid())
                .name(service.getName())
                .maxProfiles(service.getMaxProfiles())
                .details(detailsStr)
                .createdAt(service.getCreatedAt())
                .build();
    }
}
