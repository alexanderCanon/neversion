package com.neversion.api.order.infrastructure.adapters.out.mapper;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.infrastructure.adapters.out.OrderEntity;
import com.neversion.api.shared.domain.model.enums.AccountPreference;

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
                .accountPreference(entity.getAccountPreference())
                .notes(entity.getNotes())
                .receiptUrl(entity.getReceiptUrl())
                .total(entity.getTotal())
                .discount(entity.getDiscount())
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
                .accountPreference(domain.getAccountPreference())
                .notes(domain.getNotes())
                .receiptUrl(domain.getReceiptUrl())
                .total(domain.getTotal())
                .discount(domain.getDiscount())
                .approvedAt(domain.getApprovedAt() != null
                        ? domain.getApprovedAt().atOffset(ZoneOffset.UTC) : null)
                .build() : null;
    }
}

