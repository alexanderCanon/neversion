package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadReceiptService unit tests")
class UploadReceiptServiceUT {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    private UploadReceiptService uploadReceiptService;

    @BeforeEach
    void setUp() {
        uploadReceiptService = new UploadReceiptService(reservationRepositoryPort);
    }

    private Reservation buildReservation(UUID id, ReservationStatus status) {
        return Reservation.builder()
                .id(id)
                .clientId(1L)
                .discount(BigDecimal.ZERO)
                .total(new BigDecimal("100.00"))
                .status(status)
                .expirationDate(Instant.now().plus(60, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("uploadReceipt - should transition to UPLOADED when status is PENDING (BR-05)")
    void uploadReceipt_shouldTransitionToUploaded_whenStatusIsPending() {
        // Given
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = buildReservation(reservationId, ReservationStatus.PENDING);
        String receiptUrl = "https://s3.example.com/receipt-123.jpg";

        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.existsByReceiptUrl(receiptUrl)).thenReturn(false);
        when(reservationRepositoryPort.update(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = uploadReceiptService.uploadReceipt(reservationId, receiptUrl);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.UPLOADED);
        assertThat(result.getReceiptUrl()).isEqualTo(receiptUrl);
    }

    @Test
    @DisplayName("uploadReceipt - should throw BusinessRuleException when status is not PENDING")
    void uploadReceipt_shouldThrowBusinessRuleException_whenStatusIsNotPending() {
        // Given
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = buildReservation(reservationId, ReservationStatus.VALIDATED);

        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> uploadReceiptService.uploadReceipt(reservationId, "https://receipt.jpg"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot upload receipt for a reservation with status");
    }

    @Test
    @DisplayName("uploadReceipt - should throw BusinessRuleException when receipt URL already exists (BR-05)")
    void uploadReceipt_shouldThrowBusinessRuleException_whenReceiptUrlAlreadyExists() {
        // Given
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = buildReservation(reservationId, ReservationStatus.PENDING);
        String duplicateUrl = "https://s3.example.com/duplicate-receipt.jpg";

        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.existsByReceiptUrl(duplicateUrl)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> uploadReceiptService.uploadReceipt(reservationId, duplicateUrl))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("receipt URL provided has already been used");
    }

    @Test
    @DisplayName("uploadReceipt - should throw ResourceNotFoundException when reservation not found")
    void uploadReceipt_shouldThrowResourceNotFoundException_whenReservationNotFound() {
        // Given
        UUID reservationId = UUID.randomUUID();

        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> uploadReceiptService.uploadReceipt(reservationId, "https://receipt.jpg"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }
}
