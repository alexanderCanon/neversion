package com.neversion.api.reservation.infrastructure.adapters.out;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * US-010: PK Long, reservationId Long.
 */
interface SpringDataReservationDetailRepository extends JpaRepository<ReservationDetailEntity, Long> {

    List<ReservationDetailEntity> findByReservationId(Long reservationId);
}
