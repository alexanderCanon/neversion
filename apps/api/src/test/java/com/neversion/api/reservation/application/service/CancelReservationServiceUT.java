package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.ReversePointsUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelReservationService unit tests")
class CancelReservationServiceUT {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private ReversePointsUseCase reversePointsUseCase;

    private CancelReservationService cancelReservationService;

    private static final UUID RESERVATION_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cancelReservationService = new CancelReservationService(reservationRepositoryPort, reversePointsUseCase);
    }

    private Reservation buildReservation(ReservationStatus status) {
        return Reservation.builder()
                .id(1L)
                .uuid(RESERVATION_UUID)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("cancel_pendingReservation_shouldTransitionToCancelled")
        void cancel_pendingReservation_shouldTransitionToCancelled() {
            Reservation reservation = buildReservation(ReservationStatus.PENDING);
            Reservation cancelled = buildReservation(ReservationStatus.CANCELLED);

            when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
            when(reservationRepositoryPort.update(any(Reservation.class))).thenReturn(cancelled);

            Reservation result = cancelReservationService.cancel(RESERVATION_UUID);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancel_uploadedReservation_shouldTransitionToCancelled")
        void cancel_uploadedReservation_shouldTransitionToCancelled() {
            Reservation reservation = buildReservation(ReservationStatus.UPLOADED);
            Reservation cancelled = buildReservation(ReservationStatus.CANCELLED);

            when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
            when(reservationRepositoryPort.update(any(Reservation.class))).thenReturn(cancelled);

            Reservation result = cancelReservationService.cancel(RESERVATION_UUID);

            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancel_notFound_shouldThrow404")
        void cancel_notFound_shouldThrow404() {
            when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelReservationService.cancel(RESERVATION_UUID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(RESERVATION_UUID.toString());
        }

        @Test
        @DisplayName("cancel_validatedReservation_shouldThrow409")
        void cancel_validatedReservation_shouldThrow409() {
            when(reservationRepositoryPort.findByUuid(RESERVATION_UUID))
                    .thenReturn(Optional.of(buildReservation(ReservationStatus.VALIDATED)));

            assertThatThrownBy(() -> cancelReservationService.cancel(RESERVATION_UUID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("PENDING or UPLOADED");
        }

        @Test
        @DisplayName("cancel_alreadyCancelledReservation_shouldThrow409")
        void cancel_alreadyCancelledReservation_shouldThrow409() {
            when(reservationRepositoryPort.findByUuid(RESERVATION_UUID))
                    .thenReturn(Optional.of(buildReservation(ReservationStatus.CANCELLED)));

            assertThatThrownBy(() -> cancelReservationService.cancel(RESERVATION_UUID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("PENDING or UPLOADED");
        }

        @Test
        @DisplayName("cancel_rejectedReservation_shouldThrow409")
        void cancel_rejectedReservation_shouldThrow409() {
            when(reservationRepositoryPort.findByUuid(RESERVATION_UUID))
                    .thenReturn(Optional.of(buildReservation(ReservationStatus.REJECTED)));

            assertThatThrownBy(() -> cancelReservationService.cancel(RESERVATION_UUID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("PENDING or UPLOADED");
        }
    }
}
