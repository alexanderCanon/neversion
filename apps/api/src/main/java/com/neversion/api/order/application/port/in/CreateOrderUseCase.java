package com.neversion.api.order.application.port.in;

import java.util.UUID;

import com.neversion.api.order.domain.model.Order;

/**
 * Creates an Order from a validated reservation.
 */
public interface CreateOrderUseCase {

    Order createFromReservation(Long reservationId, UUID reservationUuid, Long clientId, Long vendorId,
            String paymentMethod, String receiptUrl, java.math.BigDecimal total, java.math.BigDecimal discount, String notes);
}
