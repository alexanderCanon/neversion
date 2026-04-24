package com.neversion.api.order.infrastructure.adapters.out.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.infrastructure.adapters.out.OrderEntity;

/**
 * Explicit mapper — US-008: PK is now Long, uuid is separate column.
 */
@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderEntity entity) {
        return entity != null ? Order.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .reservationId(entity.getReservationId())
                .vendorId(entity.getVendorId())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant() : null)
                .build() : null;
    }

    public OrderEntity toEntity(Order domain) {
        return domain != null ? OrderEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .reservationId(domain.getReservationId())
                .vendorId(domain.getVendorId())
                .status(domain.getStatus())
                .notes(domain.getNotes())
                .build() : null;
    }
}
