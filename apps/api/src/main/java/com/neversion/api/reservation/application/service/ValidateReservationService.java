package com.neversion.api.reservation.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.application.port.in.CreateOrderUseCase;
import com.neversion.api.reservation.application.port.in.ValidateReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

/**
 * UC3: Validate and Formalize Sale.
 * <p>
 * Admin validates the payment receipt, transitions the reservation from
 * UPLOADED → VALIDATED, and triggers creation of the Order.
 * </p>
 */
@Service
public class ValidateReservationService implements ValidateReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final CreateOrderUseCase createOrderUseCase;

    public ValidateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            CreateOrderUseCase createOrderUseCase) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.createOrderUseCase = createOrderUseCase;
    }

    @Override
    @Transactional
    public Reservation validate(UUID reservationId, String notes) {

        Reservation reservation = reservationRepositoryPort.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + reservationId));

        // Only UPLOADED reservations can be validated
        if (reservation.getStatus() != ReservationStatus.UPLOADED) {
            throw new BusinessRuleException(
                    "Cannot validate a reservation with status: " + reservation.getStatus()
                            + ". Expected: UPLOADED");
        }

        reservation.setStatus(ReservationStatus.VALIDATED);
        Reservation updated = reservationRepositoryPort.update(reservation);

        // Create the associated Order
        createOrderUseCase.createFromReservation(reservationId, notes);

        return updated;
    }
}
