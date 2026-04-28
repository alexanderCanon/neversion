package com.neversion.api.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
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

    private ReservationPricingService reservationPricingService;
    private CreateReservationService createReservationService;

    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final Long CLIENT_ID = 10L;
    private static final Long VENDOR_ID = 5L;
    private static final Long SERVICE_ID = 1L;
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
                vendorRepositoryPort);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Client buildClient() {
        return Client.builder().id(CLIENT_ID).uuid(CLIENT_UUID).vendorId(VENDOR_ID).name("Juan").build();
    }

    private Vendor buildVendor() {
        return Vendor.builder().id(VENDOR_ID).uuid(UUID.randomUUID()).discountCfg(DISCOUNT_CFG).build();
    }

    private com.neversion.api.service.domain.model.Service buildService() {
        return com.neversion.api.service.domain.model.Service.builder()
                .id(SERVICE_ID).uuid(UUID.randomUUID()).name("Netflix")
                .priceProfile(new BigDecimal("50.00")).vendorId(VENDOR_ID).build();
    }

    private void mockFullResolution() {
        when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.of(buildService()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                .thenReturn(10L); // plenty available
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
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 1));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getVendorId()).isEqualTo(VENDOR_ID);
        assertThat(result.getPaymentMethod()).isEqualTo(PAYMENT_METHOD);
        // 1 item → below discount threshold → discount = 0
        assertThat(result.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        // total = 50.00 (1 × 50.00 price)
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("create - should apply tier 1 discount for 2 items (BR-13)")
    void create_twoItems_shouldApplyTier1Discount() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 2));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD);

        // Then — 2 items × 50 = 100, discount = 5% of 100 = 5.00, total = 95.00
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("95.00"));
    }

    @Test
    @DisplayName("create - should apply tier 2 discount for 4+ items (BR-13)")
    void create_fourItems_shouldApplyTier2Discount() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 4));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD);

        // Then — 4 items × 50 = 200, discount = 10% of 200 = 20.00, total = 180.00
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("180.00"));
    }

    @Test
    @DisplayName("create - should set expiration 60 minutes from now")
    void create_shouldSetExpiration60MinutesFromNow() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 1));

        // When
        Instant before = Instant.now().plus(59, ChronoUnit.MINUTES);
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD);
        Instant after = Instant.now().plus(61, ChronoUnit.MINUTES);

        // Then
        assertThat(result.getExpirationDate()).isAfter(before);
        assertThat(result.getExpirationDate()).isBefore(after);
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when client not found")
    void create_clientNotFound_shouldThrow404() {
        // Given
        when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 1));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when service not found")
    void create_serviceNotFound_shouldThrow404() {
        // Given
        when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.empty());
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 1));

        // When / Then
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    @DisplayName("create - should throw BusinessRuleException when not enough profiles available")
    void create_insufficientProfiles_shouldThrow400() {
        // Given
        when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));
        when(vendorRepositoryPort.findByInternalId(VENDOR_ID)).thenReturn(Optional.of(buildVendor()));
        when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.of(buildService()));
        when(profileRepositoryPort.countAvailableByServiceIdAndVendorId(SERVICE_ID, VENDOR_ID))
                .thenReturn(1L); // only 1 available, but requesting 3
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 3));

        // When / Then — BR-US033-01
        assertThatThrownBy(() -> createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Not enough available profiles");
    }

    @Test
    @DisplayName("create - should persist details with correct unit price from service catalog")
    void create_shouldPersistDetailsWithServicePrice() {
        // Given
        mockFullResolution();
        stubSaveReturnsWithId();
        List<ReservationItemCommand> items = List.of(new ReservationItemCommand(SERVICE_ID, 1));

        // When
        Reservation result = createReservationService.create(CLIENT_UUID, items, PAYMENT_METHOD);

        // Then
        assertThat(result.getDetails()).hasSize(1);
        assertThat(result.getDetails().get(0).unitPrice())
                .isEqualByComparingTo(new BigDecimal("50.00"));
    }
}
