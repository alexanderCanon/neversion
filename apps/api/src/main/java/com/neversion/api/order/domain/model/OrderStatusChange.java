package com.neversion.api.order.domain.model;

import java.time.Instant;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * US-038 CA3: Audit record for order status transitions.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChange {

    private Long id;
    private Long orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String changedBy;
    private String notes;
    private Instant changedAt;
}
