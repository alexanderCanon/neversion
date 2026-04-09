package com.neversion.api.reservation.application.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.reservation.application.port.in.CreateReservationUseCase;
import com.neversion.api.reservation.application.port.in.ReservationItemCommand;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.reservation.domain.service.ReservationPricingService;

/**
 * UC1: Create Reservation (Checkout).
 * <p>
 * The client ID is optional at creation — can be attached later via
 * PUT /reservations/{id}/client. Persists the reservation header with
 * status = PENDING, saves each item capturing the current price (BR-02),
 * and computes the total applying combo discount (BR-03).
 * </p>
 * NOTE: Unit price per item is set to ZERO pending re-integration with
 * the Service pricing model (inventory module was removed).
 */
@Service
public class CreateReservationService implements CreateReservationUseCase {

    private static final int EXPIRATION_MINUTES = 60;

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final ReservationPricingService reservationPricingService;
    private final ClientRepositoryPort clientRepositoryPort;

    public CreateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            ReservationPricingService reservationPricingService,
            ClientRepositoryPort clientRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.reservationPricingService = reservationPricingService;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    @Transactional
    public Reservation create(UUID clientId, List<ReservationItemCommand> items) {

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expirationDate = now.plusMinutes(EXPIRATION_MINUTES);

        // Build reservation details — unit price is ZERO until service pricing is integrated
        List<ReservationDetail> detailsToSave = new ArrayList<>();
        for (ReservationItemCommand item : items) {
            detailsToSave.add(new ReservationDetail(
                    null,
                    null, // reservationId set after save
                    item.inventoryId(),
                    item.qty(),
                    BigDecimal.ZERO,
                    null)); // subtotal is DB-computed
        }

        // Calculate pricing using domain service (BR-03)
        BigDecimal grossTotal = reservationPricingService.calculateGrossTotal(detailsToSave);
        BigDecimal discount = reservationPricingService.calculateComboDiscount(grossTotal, items.size());
        BigDecimal finalTotal = reservationPricingService.calculateFinalTotal(grossTotal, discount);

        // Resolve internal Client ID if UUID is provided
        Long internalClientId = null;
        if (clientId != null) {
            internalClientId = clientRepositoryPort.findById(clientId)
                    .map(Client::getId)
                    .orElse(null);
        }

        Reservation reservation = Reservation.builder()
                .clientId(internalClientId)
                .clientUuid(clientId) // nullable — can be attached later
                .status(ReservationStatus.PENDING)
                .discount(discount)
                .total(finalTotal)
                .expirationDate(expirationDate.toInstant())
                .build();

        Reservation savedReservation = reservationRepositoryPort.save(reservation);

        // Persist each detail linked to the saved reservation
        List<ReservationDetail> savedDetails = new ArrayList<>();
        for (ReservationDetail detail : detailsToSave) {
            ReservationDetail linked = new ReservationDetail(
                    null,
                    savedReservation.getId(),
                    detail.inventoryId(),
                    detail.qty(),
                    detail.unitPrice(),
                    null);
            savedDetails.add(reservationRepositoryPort.saveDetail(linked));
        }

        savedReservation.setDetails(savedDetails);
        return savedReservation;
    }
}
