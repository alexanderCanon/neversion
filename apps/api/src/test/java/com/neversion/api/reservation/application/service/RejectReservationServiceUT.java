package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.loyalty.application.port.in.ReversePointsUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejectReservationService unit tests — US-036")
class RejectReservationServiceUT {

    @Mock private ReservationRepositoryPort reservationRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ReversePointsUseCase reversePointsUseCase;

    private RejectReservationService rejectReservationService;

    private static final UUID RESERVATION_UUID = UUID.randomUUID();
    private static final String CALLER_EXTERNAL_ID = "supabase-id-123";
    private static final Long VENDOR_ID = 5L;
    private static final Long USER_ID = 10L;
    private static final Long CLIENT_ID = 20L;

    @BeforeEach
    void setUp() {
        rejectReservationService = new RejectReservationService(
                reservationRepositoryPort, notificationLogPort, userRepositoryPort, vendorRepositoryPort,
                clientRepositoryPort, reversePointsUseCase);
    }

    private Reservation buildReservation(ReservationStatus status, Long vendorId) {
        return Reservation.builder()
                .id(1L)
                .uuid(RESERVATION_UUID)
                .clientId(CLIENT_ID)
                .vendorId(vendorId)
                .discount(BigDecimal.ZERO)
                .total(new BigDecimal("100.00"))
                .status(status)
                .expirationDate(Instant.now().plus(60, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();
    }

    private User buildUser() {
        return User.builder().id(USER_ID).externalId(CALLER_EXTERNAL_ID).build();
    }

    private Vendor buildVendor() {
        return Vendor.builder().id(VENDOR_ID).userId(USER_ID).build();
    }

    private Client buildClient() {
        return Client.builder().id(CLIENT_ID).email("client@test.com").name("Juan Perez").build();
    }

    private void mockCallerResolution() {
        when(userRepositoryPort.findByExternalId(CALLER_EXTERNAL_ID)).thenReturn(Optional.of(buildUser()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildVendor()));
    }

    @Test
    @DisplayName("reject - should transition to REJECTED and notify client")
    void reject_validOwner_shouldTransitionAndNotify() {
        // Given
        mockCallerResolution();
        Reservation reservation = buildReservation(ReservationStatus.UPLOADED, VENDOR_ID);
        String reason = "Image is blurred";

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.update(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(buildClient()));

        // When
        Reservation result = rejectReservationService.reject(RESERVATION_UUID, reason, CALLER_EXTERNAL_ID);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(result.getNotes()).isEqualTo(reason);
        
        verify(notificationLogPort, times(1)).record(
                eq("RECEIPT_REJECTED"), 
                eq("client@test.com"), 
                any(String.class),
                eq("order"), any(), eq("rejected"));
    }

    @Test
    @DisplayName("reject - should throw BusinessRuleException when reason is missing")
    void reject_missingReason_shouldThrow400() {
        // When / Then
        assertThatThrownBy(() -> rejectReservationService.reject(RESERVATION_UUID, " ", CALLER_EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("rejection reason is mandatory");
    }

    @Test
    @DisplayName("reject - should throw AccessDeniedException when caller is not the owner")
    void reject_notOwner_shouldThrow403() {
        // Given
        mockCallerResolution();
        Reservation reservation = buildReservation(ReservationStatus.UPLOADED, 999L);

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> rejectReservationService.reject(RESERVATION_UUID, "reason", CALLER_EXTERNAL_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("do not have permission");
    }

    @Test
    @DisplayName("reject - should throw BusinessRuleException when status is not UPLOADED")
    void reject_invalidStatus_shouldThrow400() {
        // Given
        mockCallerResolution();
        Reservation reservation = buildReservation(ReservationStatus.PENDING, VENDOR_ID);

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> rejectReservationService.reject(RESERVATION_UUID, "reason", CALLER_EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot reject a reservation with status");
    }
}
