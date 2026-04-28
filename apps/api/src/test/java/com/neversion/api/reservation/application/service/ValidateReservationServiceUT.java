package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import com.neversion.api.order.application.port.in.CreateOrderUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateReservationService unit tests")
class ValidateReservationServiceUT {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    private ValidateReservationService validateReservationService;

    @BeforeEach
    void setUp() {
        validateReservationService = new ValidateReservationService(
                reservationRepositoryPort, createOrderUseCase);
    }

    private Reservation buildReservation(UUID uuid, ReservationStatus status) {
        return Reservation.builder()
                .id(1L)
                .uuid(uuid)
                .clientId(1L)
                .discount(BigDecimal.ZERO)
                .total(new BigDecimal("100.00"))
                .status(status)
                .expirationDate(Instant.now().plus(60, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("validate - should transition to VALIDATED and create order")
    void validate_shouldTransitionToValidated_andCreateOrder() {
        // Given
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = buildReservation(reservationId, ReservationStatus.UPLOADED);
        String notes = "Payment verified";

        when(reservationRepositoryPort.findByUuid(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.update(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = validateReservationService.validate(reservationId, notes);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.VALIDATED);
        verify(createOrderUseCase).createFromReservation(eq(1L), eq(notes));
    }

    @Test
    @DisplayName("validate - should throw BusinessRuleException when status is not UPLOADED")
    void validate_shouldThrowBusinessRuleException_whenStatusIsNotUploaded() {
        // Given
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = buildReservation(reservationId, ReservationStatus.PENDING);

        when(reservationRepositoryPort.findByUuid(reservationId)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> validateReservationService.validate(reservationId, "notes"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot validate a reservation with status")
                .hasMessageContaining("Expected: UPLOADED");
    }

    @Test
    @DisplayName("validate - should throw ResourceNotFoundException when reservation not found")
    void validate_shouldThrowResourceNotFoundException_whenReservationNotFound() {
        // Given
        UUID reservationId = UUID.randomUUID();

        when(reservationRepositoryPort.findByUuid(reservationId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> validateReservationService.validate(reservationId, "notes"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }
}
