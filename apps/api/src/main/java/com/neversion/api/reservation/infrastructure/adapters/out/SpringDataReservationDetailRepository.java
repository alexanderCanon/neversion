package com.neversion.api.reservation.infrastructure.adapters.out;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReservationDetailRepository extends JpaRepository<ReservationDetailEntity, UUID> {

    List<ReservationDetailEntity> findByReservationId(UUID reservationId);
}
