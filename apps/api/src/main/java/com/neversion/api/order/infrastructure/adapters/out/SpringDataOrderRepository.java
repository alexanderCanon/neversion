package com.neversion.api.order.infrastructure.adapters.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repo — PK is now Long (BIGINT IDENTITY, US-008).
 */
interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByUuid(UUID uuid);

    Optional<OrderEntity> findByReservationId(Long reservationId);
}
