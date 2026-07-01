package com.neversion.api.reservation.application.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.ReversePointsUseCase;
import com.neversion.api.reservation.application.port.in.RejectReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * UC4: Reject Receipt — US-036.
 * Transitions UPLOADED → REJECTED.
 * Triggers notification to the client with the rejection reason.
 */
@Service
public class RejectReservationService implements RejectReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ReversePointsUseCase reversePointsUseCase;

    public RejectReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            NotificationLogPort notificationLogPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ReversePointsUseCase reversePointsUseCase) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.reversePointsUseCase = reversePointsUseCase;
    }

    @Override
    @Transactional
    public Reservation reject(UUID reservationId, String reason, String callerExternalId) {

        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("A rejection reason is mandatory.");
        }

        // 1. Resolve caller vendor for ownership check
        User caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));
        
        Vendor vendor = vendorRepositoryPort.findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for caller"));

        // 2. Load and validate reservation
        Reservation reservation = reservationRepositoryPort.findByUuid(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + reservationId));

        // Ownership check
        if (!reservation.getVendorId().equals(vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to reject this reservation.");
        }

        // Only UPLOADED reservations can be rejected
        if (reservation.getStatus() != ReservationStatus.UPLOADED) {
            throw new BusinessRuleException(
                    "Cannot reject a reservation with status: " + reservation.getStatus()
                            + ". Only UPLOADED reservations can be rejected.");
        }

        // 3. Transition to REJECTED
        reservation.setStatus(ReservationStatus.REJECTED);
        reservation.setNotes(reason); // Now Reservation has notes field
        Reservation updated = reservationRepositoryPort.update(reservation);

        // 3b. Restore any points redeemed at checkout (loyalty program)
        reversePointsUseCase.reverseForReservation(updated.getId());

        // 4. Notify Client
        notifyClient(updated, reason);

        return updated;
    }

    private void notifyClient(Reservation reservation, String reason) {
        if (reservation.getClientId() == null) return;

        // FIXED: use findByInternalId instead of findById(UUID)
        clientRepositoryPort.findByInternalId(reservation.getClientId())
                .ifPresent(client -> {
                    String payload = String.format(
                            "{\"reservationId\":\"%s\",\"clientName\":\"%s\",\"reason\":\"%s\"}",
                            reservation.getUuid(), client.getName(), reason);
                    
                    notificationLogPort.record("RECEIPT_REJECTED", client.getEmail(), payload,
                            "order", reservation.getId(), "rejected");
                });
    }
}
