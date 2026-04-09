package com.neversion.api.order.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.neversion.api.order.domain.model.Order;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByReservationId(UUID reservationId);
}
