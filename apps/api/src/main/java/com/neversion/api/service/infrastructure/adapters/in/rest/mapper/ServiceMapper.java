package com.neversion.api.service.infrastructure.adapters.in.rest.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceRequest;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between ServiceRequest/ServiceResponse DTOs and the Service domain model.
 * Manual mapping — no MapStruct per project conventions.
 */
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
                throw new IllegalArgumentException(
                        "'details' field contains invalid JSON: " + e.getMessage());
            }
        }

        return Service.builder()
                .name(request.name())
                .category(request.category())
                .priceProfile(request.priceProfile())
                .priceFull(request.priceComplete())
                .durationDays(request.durationDays())
                .maxProfiles(request.maxProfiles())
                .description(request.description())
                .imageUrl(request.imageUrl())
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
                .category(service.getCategory())
                .description(service.getDescription())
                .imageUrl(service.getImageUrl())
                .priceProfile(service.getPriceProfile())
                .priceComplete(service.getPriceFull())
                .durationDays(service.getDurationDays())
                .maxProfiles(service.getMaxProfiles())
                .isActive(service.getIsActive())
                .details(detailsStr)
                .createdAt(service.getCreatedAt())
                .build();
    }
}
