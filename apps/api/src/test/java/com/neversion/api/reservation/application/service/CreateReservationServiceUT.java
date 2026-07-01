package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.RedeemPointsUseCase;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.reservation.application.port.in.ReservationItemCommand;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.reservation.domain.service.ReservationPricingService;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;

/**
 * Unit tests for CreateReservationService — US-033.
 * Convention: method_scenario_expected
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateReservationService — US-033 unit tests")
class CreateReservationServiceUT {

    @Mock private ReservationRepositoryPort reservationRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private RedeemPointsUseCase redeemPointsUseCase;

    private ReservationPricingService reservationPricingService;
    private CreateReservationService createReservationService;

    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final Long CLIENT_ID = 10L;
    private static final Long VENDOR_ID = 5L;
    private static final Long USER_ID = 20L;
    private static final UUID SERVICE_UUID = UUID.randomUUID();
    private static final Long SERVICE_ID = 1L;
    private static final UUID SERVICE_UUID_2 = UUID.randomUUID();
    private static final Long SERVICE_ID_2 = 2L;
    private static final UUID SERVICE_UUID_3 = UUID.randomUUID();
    private static final Long SERVICE_ID_3 = 3L;
    private static final UUID SERVICE_UUID_4 = UUID.randomUUID();
    private static final Long SERVICE_ID_4 = 4L;
    private static final String PAYMENT_METHOD = "transferencia";
    private static final String DISCOUNT_CFG = """
            {"min_items": 2, "tiers": [{"from": 2, "to": 3, "discount_pct": 5}, {"from": 4, "to": null, "discount_pct": 10}]}
            """;

    @BeforeEach
    void setUp() {
        reservationPricingService = new ReservationPricingService();
        createReservationService = new CreateReservationService(
                reservationRepositoryPort,
                reservationPricingService,
                clientRepositoryPort,
                serviceRepositoryPort,
                profileRepositoryPort,
                vendorRepositoryPort,
                userRepositoryPort,
                redeemPointsUseCase);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Client buildClient() {
        return Client.builder().id(CLIENT_ID).uuid(CLIENT_UUID).userId(USER_ID).vendorId(VENDOR_ID).name("Juan").build();
    }

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .externalId(CLIENT_UUID.toString())
                .role(UserRole.CLIENT)
                .build();
    }

    private Vendor buildVendor() {
        return Vendor.builder().id(VENDOR_ID).uuid(UUID.randomUUID()).discountCfg(DISCOUNT_CFG).build();
    }

    private com.neversion.api.service.domain.model.Service buildService() {
        return com.neversion.api.service.domain.model.Service.builder()
                .id(SERVICE_ID).uuid(SERVICE_UUID).name("Netflix")
                .priceProfile(new BigDecimal("50.00")).priceFull(new BigDecimal("150.00"))
                .vendorId(VENDOR_ID).build();
    }

    private com.neversion.api.service.domain.model.Service buildService2() {
        return com.neversion.api.service.domain.model.Service.builder()
                .id(SERVICE_ID_2).uuid(SERVICE_UUID_2).name("Spotify")
                .priceProfile(new BigDecimal("30.00")).priceFull(new BigDecimal("100.00"))
                .vendorId(VENDOR_ID).build();
    }

    private com.neversion.api.service.domain.model.Service buildService3() {
        return com.neversion.api.service.domain.model.Service.builder()
                .id(SERVICE_ID_3).uuid(SERVICE_UUID_3).name("Disney+")
                .priceProfile(new BigDecimal("40.00")).priceFull(new BigDecimal("120.00"))
                .vendorId(VENDOR_ID).build();
    }

    private com.neversion.api.service.domain.model.Service buildService4() {
        return com.neversion.api.service.domain.model.Service.builder()
                .id(SERVICE_ID_4).uuid(SERVICE_UUID_4).name("Max")
                .priceProfile(new BigDecimal("45.00")).priceFull(new BigDecimal("130.00"))
                .vendorId(VENDOR_ID).build();
    }

    private void mockFullResolution() {
        when(userRepositoryPort.findByExternalId(CLIENT_UUID.toString())).thenReturn(Optional.of(buildUser()));
        when(clientRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(buildService()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                .thenReturn(10L);
    }

    private void mockAllServices() {
        when(serviceRepositoryPort.findById(SERVICE_UUID_2)).thenReturn(Optional.of(buildService2()));
        when(serviceRepositoryPort.findById(SERVICE_UUID_3)).thenReturn(Optional.of(buildService3()));
        when(serviceRepositoryPort.findById(SERVICE_UUID_4)).thenReturn(Optional.of(buildService4()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID_2, VENDOR_ID))
                .thenReturn(10L);
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID_3, VENDOR_ID))
                .thenReturn(10L);
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID_4, VENDOR_ID))
                .thenReturn(10L);
    }

    private void stubSaveReturnsWithId() {
        when(reservationRepositoryPort.save(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation r = invocation.getArgument(0);
                    r.setId(1L);
                    r.setUuid(UUID.randomUUID());
                    r.setCreatedAt(Instant.now());
                    return r;
                });
        when(reservationRepositoryPort.saveDetail(any(ReservationDetail.class)))
                .thenAnswer(invocation -> {
                    ReservationDetail d = invocation.getArgument(0);
                    return new ReservationDetail(
                            1L, UUID.randomUUID(), d.reservationId(),
                            d.serviceId(), d.qty(), d.unitPrice(), d.subtotal());
                });
    }

    // ── tests ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create - should return reservation with PENDING status and correct pricing")
    void create_validRequest_shouldReturnPendingReservation() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getVendorId()).isEqualTo(VENDOR_ID);
        assertThat(result.getPaymentMethod()).isEqualTo(PAYMENT_METHOD);
        // 1 profile → below discount threshold → discount = 0
        assertThat(result.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        // total = 50.00 (1 × 50.00 priceProfile)
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("create - should apply tier 1 discount for 2 distinct profiles (BR-13)")
    void create_twoDistinctProfiles_shouldApplyTier1Discount() {
        // Given
        mockFullResolution();
        when(serviceRepositoryPort.findById(SERVICE_UUID_2)).thenReturn(Optional.of(buildService2()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID_2, VENDOR_ID))
                .thenReturn(10L);
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_2, 1, SaleMode.BY_PROFILE));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);

        // Then — 2 profiles: 50 + 30 = 80, discount = 5% of 80 = 4.00, total = 76.00
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("76.00"));
    }

    @Test
    @DisplayName("create - should apply tier 2 discount for 4 distinct profiles (BR-13)")
    void create_fourDistinctProfiles_shouldApplyTier2Discount() {
        // Given
        mockFullResolution();
        mockAllServices();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_2, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_3, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_4, 1, SaleMode.BY_PROFILE));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);

        // Then — 4 profiles: 50+30+40+45 = 165, discount = 10% of 165 = 16.50, total = 148.50
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("16.50"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("148.50"));
    }

    @Test
    @DisplayName("create - should set expiration 60 minutes from now")
    void create_shouldSetExpiration60MinutesFromNow() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When
        Instant before = Instant.now().plus(59, ChronoUnit.MINUTES);
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);
        Instant after = Instant.now().plus(61, ChronoUnit.MINUTES);

        // Then
        assertThat(result.getExpirationDate()).isAfter(before);
        assertThat(result.getExpirationDate()).isBefore(after);
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when user not found")
    void create_userNotFound_shouldThrow404() {
        // Given
        when(userRepositoryPort.findByExternalId(CLIENT_UUID.toString())).thenReturn(Optional.empty());
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when service not found")
    void create_serviceNotFound_shouldThrow404() {
        // Given
        when(userRepositoryPort.findByExternalId(CLIENT_UUID.toString())).thenReturn(Optional.of(buildUser()));
        when(clientRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.empty());
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("create - should throw BusinessRuleException when not enough profiles available")
    void create_insufficientProfiles_shouldThrow400() {
        // Given
        when(userRepositoryPort.findByExternalId(CLIENT_UUID.toString())).thenReturn(Optional.of(buildUser()));
        when(clientRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(buildService()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                .thenReturn(1L); // only 1 available, but requesting 3
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 3, SaleMode.BY_PROFILE));

        // When / Then — BR-US033-01
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Not enough available profiles");
    }

    @Test
    @DisplayName("create - should persist details with correct unit price from service catalog")
    void create_shouldPersistDetailsWithServicePrice() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);

        // Then
        assertThat(result.getDetails()).hasSize(1);
        assertThat(result.getDetails().get(0).unitPrice())
                .isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ── BR-13 v2 validation tests ────────────────────────────────────────────

    @Test
    @DisplayName("create - should throw BusinessRuleException for duplicate service (BR-13 v2)")
    void create_duplicateService_shouldThrow400() {
        // Given
        mockFullResolution();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Duplicate service in cart");
    }

    @Test
    @DisplayName("create - should throw BusinessRuleException for 5+ profiles (BR-13 v2 max 4)")
    void create_fiveProfiles_shouldThrow400() {
        // Given
        mockFullResolution();
        mockAllServices();
        UUID uuid5 = UUID.randomUUID();
        Long id5 = 5L;
        when(serviceRepositoryPort.findById(uuid5)).thenReturn(Optional.of(
                com.neversion.api.service.domain.model.Service.builder()
                        .id(id5).uuid(uuid5).name("Prime")
                        .priceProfile(new BigDecimal("35.00")).vendorId(VENDOR_ID).build()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(id5, VENDOR_ID))
                .thenReturn(10L);

        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_2, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_3, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(SERVICE_UUID_4, 1, SaleMode.BY_PROFILE),
                new ReservationItemCommand(uuid5, 1, SaleMode.BY_PROFILE));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("more than 4 profile services");
    }

    @Test
    @DisplayName("create - should throw BusinessRuleException when FULL_ACCOUNT combined with other items (BR-13 v2)")
    void create_fullAccountWithOtherItems_shouldThrow400() {
        // Given — only service 1 is looked up before the FULL_ACCOUNT validation throws
        when(userRepositoryPort.findByExternalId(CLIENT_UUID.toString())).thenReturn(Optional.of(buildUser()));
        when(clientRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(buildService()));
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.FULL_ACCOUNT),
                new ReservationItemCommand(SERVICE_UUID_2, 1, SaleMode.BY_PROFILE));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("full account purchase cannot be combined");
    }

    @Test
    @DisplayName("create - should use priceFull for FULL_ACCOUNT and apply no discount (BR-14)")
    void create_fullAccountOnly_shouldUsePriceFullNoDiscount() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.FULL_ACCOUNT));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);

        // Then — priceFull = 150.00, no discount
        assertThat(result.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getDetails().get(0).unitPrice())
                .isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("create - should use priceProfile for BY_PROFILE (BR-14)")
    void create_byProfile_shouldUsePriceProfile() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(
                new ReservationItemCommand(SERVICE_UUID, 1, SaleMode.BY_PROFILE));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD, null, null, null);

        // Then — priceProfile = 50.00
        assertThat(result.getDetails().get(0).unitPrice())
                .isEqualByComparingTo(new BigDecimal("50.00"));
    }
}
