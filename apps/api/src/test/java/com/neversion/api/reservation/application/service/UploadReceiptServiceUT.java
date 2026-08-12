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

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadReceiptService unit tests — US-034")
class UploadReceiptServiceUT {

    @Mock private ReservationRepositoryPort reservationRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private AuthServicePort authServicePort;

    private UploadReceiptService uploadReceiptService;

    private static final UUID RESERVATION_UUID = UUID.randomUUID();
    private static final String RECEIPT_URL = "https://s3.example.com/receipt-123.jpg";
    private static final Long VENDOR_ID = 5L;
    private static final Long USER_ID = 10L;

    @BeforeEach
    void setUp() {
        uploadReceiptService = new UploadReceiptService(
                reservationRepositoryPort,
                notificationLogPort,
                vendorRepositoryPort,
                userRepositoryPort,
                clientRepositoryPort,
                authServicePort);
    }

    private Reservation buildReservation(ReservationStatus status) {
        return Reservation.builder()
                .id(1L)
                .uuid(RESERVATION_UUID)
                .clientId(1L)
                .vendorId(VENDOR_ID)
                .discount(BigDecimal.ZERO)
                .total(new BigDecimal("100.00"))
                .status(status)
                .expirationDate(Instant.now().plus(60, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();
    }

    private Vendor buildVendor() {
        return Vendor.builder()
                .id(VENDOR_ID)
                .uuid(UUID.randomUUID())
                .userId(USER_ID)
                .storeName("Test Store")
                .build();
    }

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .externalId("supabase-id-123")
                .build();
    }


    private Client buildClient() {
        return Client.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .name("Client Name")
                .email("client@example.com")
                .build();
    }

    @Test
    @DisplayName("uploadReceipt - should transition to UPLOADED and notify client and vendor (US-034)")
    void uploadReceipt_validRequest_shouldTransitionAndNotify() {
        // Given
        Reservation reservation = buildReservation(ReservationStatus.PENDING);
        Vendor vendor = buildVendor();
        User user = buildUser();
        Client client = buildClient();

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.existsByReceiptUrl(RECEIPT_URL)).thenReturn(false);
        when(reservationRepositoryPort.update(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(vendor));
        when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(user));
        when(clientRepositoryPort.findByInternalId(1L)).thenReturn(Optional.of(client));
        when(authServicePort.findEmailByExternalId("supabase-id-123")).thenReturn(Optional.of("vendor@example.com"));

        // When
        Reservation result = uploadReceiptService.uploadReceipt(RESERVATION_UUID, RECEIPT_URL);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.UPLOADED);
        assertThat(result.getReceiptUrl()).isEqualTo(RECEIPT_URL);
        
        // Verify client notification
        verify(notificationLogPort, times(1)).record(
                eq("RECEIPT_UPLOADED"), 
                eq("client@example.com"), 
                any(String.class),
                eq("order"), any(), eq("receipt_uploaded"));

        // Verify vendor notification
        verify(notificationLogPort, times(1)).record(
                eq("VENDOR_RECEIPT_UPLOADED"), 
                eq("vendor@example.com"), 
                any(String.class),
                eq("order"), any(), eq("receipt_uploaded"));
    }

    @Test
    @DisplayName("uploadReceipt - should throw BusinessRuleException when status is not PENDING (terminal or expired)")
    void uploadReceipt_invalidStatus_shouldThrow400() {
        // Given
        Reservation reservation = buildReservation(ReservationStatus.CANCELLED);
        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> uploadReceiptService.uploadReceipt(RESERVATION_UUID, RECEIPT_URL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot upload receipt. Reservation is in status");
    }

    @Test
    @DisplayName("uploadReceipt - should throw BusinessRuleException when receipt URL already exists (BR-05)")
    void uploadReceipt_duplicateReceipt_shouldThrow400() {
        // Given
        Reservation reservation = buildReservation(ReservationStatus.PENDING);
        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.existsByReceiptUrl(RECEIPT_URL)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> uploadReceiptService.uploadReceipt(RESERVATION_UUID, RECEIPT_URL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("receipt URL provided has already been used");
    }

    @Test
    @DisplayName("uploadReceipt - should throw ResourceNotFoundException when reservation not found")
    void uploadReceipt_notFound_shouldThrow404() {
        // Given
        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> uploadReceiptService.uploadReceipt(RESERVATION_UUID, RECEIPT_URL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }
}
