package com.neversion.api.order.application.port.in;

import java.util.Optional;
import java.util.UUID;

import com.neversion.api.order.domain.model.Order;

/**
 * Retrieves orders by ID or by linked reservation.
 */
public interface GetOrderUseCase {

    Optional<Order> getByUuid(UUID uuid);

    Optional<Order> getByReservationId(Long reservationId);
}
