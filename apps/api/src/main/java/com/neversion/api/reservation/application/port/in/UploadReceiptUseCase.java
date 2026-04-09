package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

/**
 * UC2: Report Payment — customer uploads receipt URL.
 */
public interface UploadReceiptUseCase {

    Reservation uploadReceipt(UUID reservationId, String receiptUrl);
}
