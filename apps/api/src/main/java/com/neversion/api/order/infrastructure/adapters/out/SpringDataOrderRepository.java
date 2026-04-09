package com.neversion.api.order.infrastructure.adapters.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByReservationId(UUID reservationId);
}
