package com.neversion.api.loyalty.infrastructure.adapters.out.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.infrastructure.adapters.out.PointsLedgerEntity;

@Component
public class PointsLedgerPersistenceMapper {

    public PointsLedgerEntry toDomain(PointsLedgerEntity entity) {
        if (entity == null) return null;
        return PointsLedgerEntry.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .clientId(entity.getClientId())
                .vendorId(entity.getVendorId())
                .orderId(entity.getOrderId())
                .reservationId(entity.getReservationId())
                .entryType(entity.getEntryType())
                .status(entity.getStatus())
                .points(entity.getPoints())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant() : null)
                .build();
    }

    public PointsLedgerEntity toEntity(PointsLedgerEntry domain) {
        if (domain == null) return null;
        return PointsLedgerEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .clientId(domain.getClientId())
                .vendorId(domain.getVendorId())
                .orderId(domain.getOrderId())
                .reservationId(domain.getReservationId())
                .entryType(domain.getEntryType())
                .status(domain.getStatus())
                .points(domain.getPoints())
                .notes(domain.getNotes())
                .createdBy(domain.getCreatedBy())
                .build();
    }
}
