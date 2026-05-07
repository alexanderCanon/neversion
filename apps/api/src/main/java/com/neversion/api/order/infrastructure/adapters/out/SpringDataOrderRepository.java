package com.neversion.api.order.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repo — PK is now Long (BIGINT IDENTITY, US-008).
 */
interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByUuid(UUID uuid);

    Optional<OrderEntity> findByReservationId(Long reservationId);

    /**
     * US-030 — Historial de órdenes de un cliente.
     * Joins orders con reservations usando native query para evitar referencias
     * cruzadas de entidades entre módulos JPA.
     */
    @Query(value = "SELECT o.* FROM orders o "
            + "JOIN reservations r ON o.reservation_id = r.id "
            + "WHERE r.client_id = :clientId "
            + "ORDER BY o.created_at DESC",
            nativeQuery = true)
    List<OrderEntity> findByClientId(@Param("clientId") Long clientId);

}
