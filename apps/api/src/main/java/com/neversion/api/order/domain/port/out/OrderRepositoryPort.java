package com.neversion.api.order.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.neversion.api.order.domain.model.Order;

/**
 * Outbound port for order persistence.
 * US-008: findById now uses UUID (external identifier).
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findByUuid(UUID uuid);

    Optional<Order> findByReservationId(UUID reservationId);
}
