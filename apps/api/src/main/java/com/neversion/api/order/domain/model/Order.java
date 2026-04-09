package com.neversion.api.order.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Order {

    private UUID id;
    private UUID reservationId;
    private OrderStatus status;
    private String notes;
    private Instant createdAt;

    public Order() {
    }

    public Order(UUID id, UUID reservationId, OrderStatus status,
            String notes, Instant createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
