package com.neversion.api.reservation.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

public interface ReservationRepositoryPort {

    boolean existsByReceiptUrl(String receiptUrl);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    Optional<Reservation> findById(UUID id);

    List<Reservation> findAll();

    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Bulk-updates all PENDING reservations whose expiration_date has passed
     * to EXPIRED status. Returns the count of affected rows.
     */
    int expirePendingReservations();

    ReservationDetail saveDetail(ReservationDetail detail);

    List<ReservationDetail> findDetailsByReservationId(UUID reservationId);
}
