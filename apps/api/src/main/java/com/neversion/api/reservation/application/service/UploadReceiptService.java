package com.neversion.api.reservation.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.reservation.application.port.in.UploadReceiptUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * UC2: Report Payment — US-034.
 * <p>
 * The customer uploads the receipt image URL. The system validates:
 * 1. Reservation is PENDING (not terminal or expired).
 * 2. Anti-fraud (BR-05: unique receipt_url).
 * Transitions: PENDING → UPLOADED.
 * Triggers: RECEIPT_UPLOADED notification for the vendor.
 * </p>
 */
@Service
public class UploadReceiptService implements UploadReceiptUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public UploadReceiptService(
            ReservationRepositoryPort reservationRepositoryPort,
            NotificationLogPort notificationLogPort,
            VendorRepositoryPort vendorRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional
    public Reservation uploadReceipt(UUID reservationId, String receiptUrl) {

        // 1. Load and validate reservation
        Reservation reservation = reservationRepositoryPort.findByUuid(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + reservationId));

        // US-034 AC: Cannot upload if expired or terminal
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException(
                    "Cannot upload receipt. Reservation is in status: " + reservation.getStatus()
                            + ". Only PENDING reservations can receive a receipt.");
        }

        // BR-05: Anti-fraud — same receipt URL cannot be used twice
        if (reservationRepositoryPort.existsByReceiptUrl(receiptUrl)) {
            throw new BusinessRuleException(
                    "The receipt URL provided has already been used. Please upload a different payment receipt.");
        }

        // 2. Update status and receipt URL
        reservation.setReceiptUrl(receiptUrl);
        reservation.setStatus(ReservationStatus.UPLOADED);
        Reservation updatedReservation = reservationRepositoryPort.update(reservation);

        // 3. Notify Vendor (US-034 AC)
        notifyVendor(updatedReservation);

        return updatedReservation;
    }

    private void notifyVendor(Reservation reservation) {
        // Resolve vendor and user to get externalId
        Vendor vendor = vendorRepositoryPort.findByInternalId(reservation.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for reservation: " + reservation.getId()));

        User user = userRepositoryPort.findById(vendor.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for vendor: " + vendor.getId()));

        // Since vendor email is not stored in DB, we use a placeholder and include externalId
        // The notification agent (n8n) will use externalId to resolve the email in Supabase.
        String payload = String.format(
                "{\"reservationId\":\"%s\",\"vendorId\":\"%s\",\"vendorExternalId\":\"%s\",\"storeName\":\"%s\",\"total\":\"%s\"}",
                reservation.getUuid(), vendor.getUuid(), user.getExternalId(),
                vendor.getStoreName(), reservation.getTotal());

        notificationLogPort.record("RECEIPT_UPLOADED", "notify-vendor@neversion.local", payload);
    }
}
