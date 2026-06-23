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
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.application.port.in.CreateOrderUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountPreference;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.application.port.in.RenewSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateReservationService unit tests — US-035")
class ValidateReservationServiceUT {

    @Mock private ReservationRepositoryPort reservationRepositoryPort;
    @Mock private CreateOrderUseCase createOrderUseCase;
    @Mock private RenewSubscriptionUseCase renewSubscriptionUseCase;
    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private OrderStatusHistoryPort orderStatusHistoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;

    private ValidateReservationService validateReservationService;

    private static final UUID RESERVATION_UUID = UUID.randomUUID();
    private static final String CALLER_EXTERNAL_ID = "supabase-id-123";
    private static final Long VENDOR_ID = 5L;
    private static final Long USER_ID = 10L;
    private static final Long CLIENT_ID = 1L;
    private static final String PAYMENT_METHOD = "transferencia";
    private static final String STORE_NAME = "Test Store";

    @BeforeEach
    void setUp() {
        validateReservationService = new ValidateReservationService(
                reservationRepositoryPort, createOrderUseCase, renewSubscriptionUseCase,
                subscriptionRepositoryPort, orderRepositoryPort, orderStatusHistoryPort,
                userRepositoryPort, vendorRepositoryPort, clientRepositoryPort, notificationLogPort);
    }

    private Reservation buildReservation(ReservationStatus status, Long vendorId) {
        return Reservation.builder()
                .id(1L)
                .uuid(RESERVATION_UUID)
                .clientId(CLIENT_ID)
                .vendorId(vendorId)
                .discount(BigDecimal.ZERO)
                .total(new BigDecimal("100.00"))
                .paymentMethod(PAYMENT_METHOD)
                .status(status)
                .expirationDate(Instant.now().plus(60, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();
    }

    private User buildUser() {
        return User.builder().id(USER_ID).externalId(CALLER_EXTERNAL_ID).build();
    }

    private Vendor buildVendor() {
        return Vendor.builder().id(VENDOR_ID).userId(USER_ID).storeName(STORE_NAME).build();
    }

    private void mockCallerResolution() {
        when(userRepositoryPort.findByExternalId(CALLER_EXTERNAL_ID)).thenReturn(Optional.of(buildUser()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildVendor()));
    }

    @Test
    @DisplayName("validate - should transition to VALIDATED, create order, and notify client")
    void validate_validOwner_shouldTransitionCreateOrderAndNotify() {
        // Given
        mockCallerResolution();
        Reservation reservation = buildReservation(ReservationStatus.UPLOADED, VENDOR_ID);
        String notes = "Payment verified";

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.update(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepositoryPort.findByInternalId(CLIENT_ID))
                .thenReturn(Optional.of(Client.builder().id(CLIENT_ID).name("Juan").email("juan@test.com").build()));

        // When
        Reservation result = validateReservationService.validate(RESERVATION_UUID, notes, CALLER_EXTERNAL_ID);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.VALIDATED);

        // Verify order creation with total and discount
        verify(createOrderUseCase).createFromReservation(
                eq(1L), eq(RESERVATION_UUID), eq(CLIENT_ID), eq(VENDOR_ID), eq(PAYMENT_METHOD),
                eq(null), eq(null), eq(new BigDecimal("100.00")), eq(BigDecimal.ZERO), eq(notes));

        // Verify PAYMENT_APPROVED notification (US-035 CA4)
        verify(notificationLogPort).record(eq("PAYMENT_APPROVED"), eq("juan@test.com"), any(String.class),
                eq("order"), any(), eq("approved"));
    }

    @Test
    @DisplayName("validate - should renew subscription and complete order for renewal reservation")
    void validate_renewalReservation_shouldRenewSubscriptionAndCompleteOrder() {
        // Given
        mockCallerResolution();
        UUID subscriptionUuid = UUID.randomUUID();
        Long subscriptionId = 77L;
        String notes = "Renewal payment verified";

        Reservation reservation = buildReservation(ReservationStatus.UPLOADED, VENDOR_ID);
        reservation.setRenewalSubscriptionId(subscriptionId);

        Order createdOrder = Order.builder()
                .id(11L)
                .uuid(UUID.randomUUID())
                .status(OrderStatus.VALIDATED)
                .build();

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .uuid(subscriptionUuid)
                .vendorId(VENDOR_ID)
                .build();

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.update(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(createOrderUseCase.createFromReservation(
                eq(1L), eq(RESERVATION_UUID), eq(CLIENT_ID), eq(VENDOR_ID), eq(PAYMENT_METHOD),
                eq(null), eq(null), eq(new BigDecimal("100.00")), eq(BigDecimal.ZERO), eq(notes)))
                .thenReturn(createdOrder);
        when(subscriptionRepositoryPort.findByInternalId(subscriptionId)).thenReturn(Optional.of(subscription));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepositoryPort.findByInternalId(CLIENT_ID))
                .thenReturn(Optional.of(Client.builder().id(CLIENT_ID).name("Juan").email("juan@test.com").build()));

        // When
        Reservation result = validateReservationService.validate(RESERVATION_UUID, notes, CALLER_EXTERNAL_ID);

        // Then
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.VALIDATED);
        verify(renewSubscriptionUseCase).renew(subscriptionUuid, CALLER_EXTERNAL_ID);
        verify(orderRepositoryPort).save(any(Order.class));
        verify(orderStatusHistoryPort).record(any(OrderStatusChange.class));
    }

    @Test
    @DisplayName("validate - should throw AccessDeniedException when caller is not the owner (ADR-02)")
    void validate_notOwner_shouldThrow403() {
        // Given
        mockCallerResolution();
        Reservation reservation = buildReservation(ReservationStatus.UPLOADED, 999L); // Different vendor

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> validateReservationService.validate(RESERVATION_UUID, "notes", CALLER_EXTERNAL_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("do not have permission");
    }

    @Test
    @DisplayName("validate - should throw BusinessRuleException when status is not UPLOADED")
    void validate_invalidStatus_shouldThrow400() {
        // Given
        mockCallerResolution();
        Reservation reservation = buildReservation(ReservationStatus.PENDING, VENDOR_ID);

        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.of(reservation));

        // When / Then
        assertThatThrownBy(() -> validateReservationService.validate(RESERVATION_UUID, "notes", CALLER_EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot validate a reservation with status")
                .hasMessageContaining("Expected: UPLOADED");
    }

    @Test
    @DisplayName("validate - should throw ResourceNotFoundException when reservation not found")
    void validate_notFound_shouldThrow404() {
        // Given
        mockCallerResolution();
        when(reservationRepositoryPort.findByUuid(RESERVATION_UUID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> validateReservationService.validate(RESERVATION_UUID, "notes", CALLER_EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }
}
