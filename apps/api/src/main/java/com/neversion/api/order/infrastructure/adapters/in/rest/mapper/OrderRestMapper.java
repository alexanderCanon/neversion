package com.neversion.api.order.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.infrastructure.adapters.in.rest.dto.OrderResponse;

/**
 * EPIC-05: Maps Order domain → OrderResponse with all enriched fields.
 */
@Component
public class OrderRestMapper {

    public OrderResponse toResponse(Order order) {
        return order != null ? OrderResponse.builder()
                .id(order.getUuid())
                .reservationId(order.getReservationUuid())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .receiptUrl(order.getReceiptUrl())
                .total(order.getTotal())
                .discount(order.getDiscount())
                .approvedAt(order.getApprovedAt())
                .createdAt(order.getCreatedAt())
                .build() : null;
    }
}

