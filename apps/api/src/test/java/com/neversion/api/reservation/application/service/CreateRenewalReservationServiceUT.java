package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
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
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateRenewalReservationService — EPIC-09 / US-061 unit tests")
class CreateRenewalReservationServiceUT {

    @Mock private ReservationRepositoryPort reservationRepositoryPort;
    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;

    private CreateRenewalReservationService createRenewalReservationService;

    private static final String CALLER_EXTERNAL_ID = "auth|client-001";
    private static final Long USER_ID = 5L;
    private static final Long CLIENT_ID = 10L;
    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final Long VENDOR_ID = 20L;
    private static final Long SUBSCRIPTION_ID = 30L;
    private static final UUID SUBSCRIPTION_UUID = UUID.randomUUID();
    private static final Long SERVICE_ID = 40L;
    private static final String PAYMENT_METHOD = "TRANSFERENCIA";

    @BeforeEach
    void setUp() {
        createRenewalReservationService = new CreateRenewalReservationService(
                reservationRepositoryPort,
                subscriptionRepositoryPort,
                userRepositoryPort,
                clientRepositoryPort);
    }

    private void mockClientResolution() {
        when(userRepositoryPort.findByExternalId(CALLER_EXTERNAL_ID))
                .thenReturn(Optional.of(User.builder()
                        .id(USER_ID)
                        .externalId(CALLER_EXTERNAL_ID)
                        .role(UserRole.CLIENT)
                        .build()));
        when(clientRepositoryPort.findByUserId(USER_ID))
                .thenReturn(Optional.of(Client.builder()
                        .id(CLIENT_ID)
                        .uuid(CLIENT_UUID)
                        .vendorId(VENDOR_ID)
                        .build()));
    }

    private Subscription buildSubscription(SubStatus status, Long clientId) {
        return Subscription.builder()
                .id(SUBSCRIPTION_ID)
                .uuid(SUBSCRIPTION_UUID)
                .clientId(clientId)
                .vendorId(VENDOR_ID)
                .serviceId(SERVICE_ID)
                .priceSold(new BigDecimal("75.00"))
                .discountApplied(new BigDecimal("5.00"))
                .status(status)
                .build();
    }

    private void stubReservationSave() {
        when(reservationRepositoryPort.save(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);
                    reservation.setId(1L);
                    reservation.setUuid(UUID.randomUUID());
                    reservation.setCreatedAt(Instant.now());
                    return reservation;
                });
        when(reservationRepositoryPort.saveDetail(any(ReservationDetail.class)))
                .thenAnswer(invocation -> {
                    ReservationDetail detail = invocation.getArgument(0);
                    return new ReservationDetail(
                            1L,
                            UUID.randomUUID(),
                            detail.reservationId(),
                            detail.serviceId(),
                            detail.qty(),
                            detail.unitPrice(),
                            detail.subtotal());
                });
    }

    @Test
    @DisplayName("create - should create renewal reservation for ACTIVE subscription")
    void create_activeSubscription_shouldCreateRenewalReservation() {
        mockClientResolution();
        when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                .thenReturn(Optional.of(buildSubscription(SubStatus.ACTIVE, CLIENT_ID)));
        when(reservationRepositoryPort.existsActiveRenewalBySubscriptionId(SUBSCRIPTION_ID))
                .thenReturn(false);
        stubReservationSave();

        Reservation result = createRenewalReservationService.create(
                SUBSCRIPTION_UUID, PAYMENT_METHOD, CALLER_EXTERNAL_ID);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getVendorId()).isEqualTo(VENDOR_ID);
        assertThat(result.getRenewalSubscriptionId()).isEqualTo(SUBSCRIPTION_ID);
        assertThat(result.getRenewalSubscriptionUuid()).isEqualTo(SUBSCRIPTION_UUID);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getDetails()).hasSize(1);
    }

    @Test
    @DisplayName("create - should create renewal reservation for SUSPENDED subscription")
    void create_suspendedSubscription_shouldCreateRenewalReservation() {
        mockClientResolution();
        when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                .thenReturn(Optional.of(buildSubscription(SubStatus.SUSPENDED, CLIENT_ID)));
        when(reservationRepositoryPort.existsActiveRenewalBySubscriptionId(SUBSCRIPTION_ID))
                .thenReturn(false);
        stubReservationSave();

        Reservation result = createRenewalReservationService.create(
                SUBSCRIPTION_UUID, PAYMENT_METHOD, CALLER_EXTERNAL_ID);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getRenewalSubscriptionUuid()).isEqualTo(SUBSCRIPTION_UUID);
    }

    @Test
    @DisplayName("create - should throw AccessDeniedException for another client's subscription")
    void create_notOwnedSubscription_shouldThrow403() {
        mockClientResolution();
        when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                .thenReturn(Optional.of(buildSubscription(SubStatus.ACTIVE, 999L)));

        assertThatThrownBy(() -> createRenewalReservationService.create(
                SUBSCRIPTION_UUID, PAYMENT_METHOD, CALLER_EXTERNAL_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("create - should throw BusinessRuleException for non-renewable status")
    void create_cancelledSubscription_shouldThrow400() {
        mockClientResolution();
        when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                .thenReturn(Optional.of(buildSubscription(SubStatus.CANCELLED, CLIENT_ID)));

        assertThatThrownBy(() -> createRenewalReservationService.create(
                SUBSCRIPTION_UUID, PAYMENT_METHOD, CALLER_EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only ACTIVE or SUSPENDED");
    }

    @Test
    @DisplayName("create - should throw BusinessRuleException when active renewal reservation exists")
    void create_existingRenewalReservation_shouldThrow400() {
        mockClientResolution();
        when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                .thenReturn(Optional.of(buildSubscription(SubStatus.ACTIVE, CLIENT_ID)));
        when(reservationRepositoryPort.existsActiveRenewalBySubscriptionId(SUBSCRIPTION_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> createRenewalReservationService.create(
                SUBSCRIPTION_UUID, PAYMENT_METHOD, CALLER_EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already an active renewal reservation");
    }
}

