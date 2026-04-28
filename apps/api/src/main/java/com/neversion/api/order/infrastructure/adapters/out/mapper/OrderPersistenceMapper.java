package com.neversion.api.order.infrastructure.adapters.out.mapper;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.infrastructure.adapters.out.OrderEntity;

/**
 * Explicit mapper — US-008: PK is now Long, uuid is separate column.
 * EPIC-05: Added clientId, paymentMethod, approvedAt mapping.
 */
@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderEntity entity) {
        return entity != null ? Order.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .reservationId(entity.getReservationId())
                .clientId(entity.getClientId())
                .vendorId(entity.getVendorId())
                .status(entity.getStatus())
                .paymentMethod(entity.getPaymentMethod())
                .notes(entity.getNotes())
                .approvedAt(entity.getApprovedAt() != null ? entity.getApprovedAt().toInstant() : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant() : null)
                .build() : null;
    }

    public OrderEntity toEntity(Order domain) {
        return domain != null ? OrderEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .reservationId(domain.getReservationId())
                .clientId(domain.getClientId())
                .vendorId(domain.getVendorId())
                .status(domain.getStatus())
                .paymentMethod(domain.getPaymentMethod())
                .notes(domain.getNotes())
                .approvedAt(domain.getApprovedAt() != null
                        ? domain.getApprovedAt().atOffset(ZoneOffset.UTC) : null)
                .build() : null;
    }
}

