package com.neversion.api.service.infrastructure.adapters.out.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.infrastructure.adapters.out.ServiceEntity;

@Component
public class ServicePersistenceMapper {

    public Service toDomain(ServiceEntity entity) {
        if (entity == null) return null;
        return Service.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .name(entity.getName())
                .vendorId(entity.getVendorId())
                .maxProfiles(entity.getMaxProfiles())
                .details(entity.getDetails())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .priceProfile(entity.getPriceProfile())
                .priceFull(entity.getPriceFull())
                .durationDays(entity.getDurationDays())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ServiceEntity toEntity(Service domain) {
        if (domain == null) return null;
        return ServiceEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .name(domain.getName())
                .vendorId(domain.getVendorId())
                .maxProfiles(domain.getMaxProfiles())
                .details(domain.getDetails())
                .category(domain.getCategory())
                .description(domain.getDescription())
                .imageUrl(domain.getImageUrl())
                .priceProfile(domain.getPriceProfile())
                .priceFull(domain.getPriceFull())
                .durationDays(domain.getDurationDays())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
