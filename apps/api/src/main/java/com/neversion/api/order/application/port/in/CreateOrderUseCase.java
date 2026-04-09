package com.neversion.api.order.application.port.in;

import java.util.UUID;

import com.neversion.api.order.domain.model.Order;

/**
 * Creates an Order from a validated reservation.
 */
public interface CreateOrderUseCase {

    Order createFromReservation(UUID reservationId, String notes);
}
