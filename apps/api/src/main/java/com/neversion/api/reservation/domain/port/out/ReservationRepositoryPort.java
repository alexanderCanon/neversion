package com.neversion.api.reservation.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

/**
 * US-009: findById→findByUuid. Details use Long reservationId.
 */
public interface ReservationRepositoryPort {

    boolean existsByReceiptUrl(String receiptUrl);

    boolean existsActiveRenewalBySubscriptionId(Long subscriptionId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    Optional<Reservation> findByUuid(UUID uuid);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByClientId(Long clientId);

    List<Reservation> findByStatus(ReservationStatus status);

    int expirePendingReservations();

    ReservationDetail saveDetail(ReservationDetail detail);

    List<ReservationDetail> findDetailsByReservationId(Long reservationId);
}
