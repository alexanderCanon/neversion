package com.neversion.api.order.domain.port.out;

import java.util.List;
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

    /** Internal lookup for cross-module details. Never expose the Long id through REST. */
    Optional<Order> findByInternalId(Long id);

    Optional<Order> findByReservationId(Long reservationId);

    /** US-030 — Historial de órdenes del cliente, ordenado por fecha desc. */
    List<Order> findByClientId(Long clientId);

    /** US-037 — Listado de órdenes para panel de vendedor con filtros. */
    List<Order> findByVendorIdFiltered(Long vendorId, Long clientId, com.neversion.api.order.domain.model.enums.OrderStatus status);
}
