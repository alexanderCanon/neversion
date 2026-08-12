package com.neversion.api.service.application.service;

import com.neversion.api.exception.BusinessRuleException;
import org.springframework.security.access.AccessDeniedException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DigitalServiceService (EPIC-02: US-017 to US-021).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DigitalServiceService unit tests")
class DigitalServiceServiceUT {

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private VendorRepositoryPort vendorRepositoryPort;

    private DigitalServiceService sut;

    private static final String EXTERNAL_ID = "supabase-user-uuid-123";
    private static final Long USER_ID = 10L;
    private static final Long VENDOR_ID = 20L;
    private static final UUID SERVICE_UUID = UUID.randomUUID();
    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final Long OTHER_VENDOR_ID = 99L;

    @BeforeEach
    void setUp() {
        sut = new DigitalServiceService(serviceRepositoryPort, userRepositoryPort, vendorRepositoryPort);
    }

    // ─── US-017: Create ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("US-017 — Create")
    class CreateTests {

        @Test
        @DisplayName("should create service with vendorId resolved from JWT subject")
        void create_shouldResolveVendorAndSave() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Service input = buildServiceInput();
            Service saved = buildSavedService(VENDOR_ID);
            when(serviceRepositoryPort.existsByName(any())).thenReturn(false);
            when(serviceRepositoryPort.save(any())).thenReturn(saved);

            Service result = sut.create(input, EXTERNAL_ID);

            assertThat(result.getVendorId()).isEqualTo(VENDOR_ID);
            assertThat(result.getIsActive()).isTrue();
            verify(serviceRepositoryPort).save(any());
        }

        @Test
        @DisplayName("should throw BusinessRuleException when name already exists")
        void create_shouldThrow_whenDuplicateName() {
            when(serviceRepositoryPort.existsByName("Netflix")).thenReturn(true);

            assertThatThrownBy(() -> sut.create(buildServiceInput(), EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Netflix");

            verify(serviceRepositoryPort, never()).save(any());
        }
    }

    // ─── US-018: Update ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("US-018 — Update")
    class UpdateTests {

        @Test
        @DisplayName("should update all editable fields when caller owns the service")
        void update_shouldUpdateFields_whenOwner() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Service existing = buildSavedService(VENDOR_ID);
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(existing));
            when(serviceRepositoryPort.save(any())).thenReturn(existing);

            Service updated = buildServiceInput();
            sut.update(SERVICE_UUID, updated, EXTERNAL_ID);

            verify(serviceRepositoryPort).save(existing);
        }

        @Test
        @DisplayName("should throw AccessDeniedException (403) when caller does not own service")
        void update_shouldThrow_whenNotOwner() {
            // Caller owns VENDOR_ID=20, service belongs to OTHER_VENDOR_ID=99
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Service existing = buildSavedService(OTHER_VENDOR_ID);
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> sut.update(SERVICE_UUID, buildServiceInput(), EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("do not own");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when service not found")
        void update_shouldThrow_whenServiceNotFound() {
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.update(SERVICE_UUID, buildServiceInput(), EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── US-019: Toggle ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("US-019 — Toggle status")
    class ToggleTests {

        @Test
        @DisplayName("should toggle isActive from true to false")
        void toggle_shouldDeactivate_whenActive() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Service active = buildSavedService(VENDOR_ID); // isActive = true
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(active));
            when(serviceRepositoryPort.save(any())).thenReturn(active);

            sut.toggleStatus(SERVICE_UUID, EXTERNAL_ID);

            assertThat(active.getIsActive()).isFalse();
            verify(serviceRepositoryPort).save(active);
        }

        @Test
        @DisplayName("should throw AccessDeniedException (403) when caller does not own service")
        void toggle_shouldThrow_whenNotOwner() {
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            Service otherVendorService = buildSavedService(OTHER_VENDOR_ID);
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(otherVendorService));

            assertThatThrownBy(() -> sut.toggleStatus(SERVICE_UUID, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ─── US-020: listByVendor ────────────────────────────────────────────────

    @Nested
    @DisplayName("US-020 — Vendor panel list")
    class ListByVendorTests {

        @Test
        @DisplayName("should return all services (no filters) when category and isActive are null")
        void listByVendor_shouldReturnAll_whenNoFilters() {
            Vendor vendor = buildVendor(VENDOR_UUID, VENDOR_ID);
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            when(serviceRepositoryPort.findAllByVendorId(VENDOR_ID))
                    .thenReturn(List.of(buildSavedService(VENDOR_ID), buildSavedService(VENDOR_ID)));

            List<Service> result = sut.listByVendor(VENDOR_UUID, null, null, EXTERNAL_ID);

            assertThat(result).hasSize(2);
            verify(serviceRepositoryPort).findAllByVendorId(VENDOR_ID);
            verify(serviceRepositoryPort, never()).findByVendorIdAndFilters(any(), any(), any());
        }

        @Test
        @DisplayName("should use filtered query when category or isActive is provided")
        void listByVendor_shouldUseFilters_whenProvided() {
            Vendor vendor = buildVendor(VENDOR_UUID, VENDOR_ID);
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);
            when(serviceRepositoryPort.findByVendorIdAndFilters(VENDOR_ID, CategoryType.STREAMING, true))
                    .thenReturn(List.of(buildSavedService(VENDOR_ID)));

            List<Service> result = sut.listByVendor(VENDOR_UUID, CategoryType.STREAMING, true, EXTERNAL_ID);

            assertThat(result).hasSize(1);
            verify(serviceRepositoryPort).findByVendorIdAndFilters(VENDOR_ID, CategoryType.STREAMING, true);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own vendor")
        void listByVendor_shouldThrow_whenCallerDoesNotOwnVendor() {
            Vendor vendor = buildVendor(VENDOR_UUID, OTHER_VENDOR_ID);
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            stubCallerChain(EXTERNAL_ID, USER_ID, VENDOR_ID);

            assertThatThrownBy(() -> sut.listByVendor(VENDOR_UUID, null, null, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ─── US-021: listActive ──────────────────────────────────────────────────

    @Nested
    @DisplayName("US-021 — Public store catalog")
    class ListActiveTests {

        @Test
        @DisplayName("should return only active services")
        void listActive_shouldReturnOnlyActive() {
            Vendor vendor = buildVendor(VENDOR_UUID, VENDOR_ID);
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            when(serviceRepositoryPort.findActiveByVendorId(VENDOR_ID))
                    .thenReturn(List.of(buildSavedService(VENDOR_ID)));

            List<Service> result = sut.listActive(VENDOR_UUID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsActive()).isTrue();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when vendor not found")
        void listActive_shouldThrow_whenVendorNotFound() {
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.listActive(VENDOR_UUID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void stubCallerChain(String externalId, Long userId, Long vendorId) {
        User user = User.builder()
                .id(userId)
                .externalId(externalId).role(UserRole.VENDOR).build();

        Vendor vendor = buildVendor(VENDOR_UUID, vendorId);
        when(userRepositoryPort.findByExternalId(externalId)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(vendor));
    }

    private Service buildServiceInput() {
        return Service.builder()
                .name("Netflix")
                .category(CategoryType.STREAMING)
                .priceProfile(BigDecimal.valueOf(45))
                .priceFull(BigDecimal.valueOf(150))
                .durationDays(30)
                .maxProfiles(5)
                .description("Streaming service")
                .build();
    }

    private Service buildSavedService(Long vendorId) {
        return Service.builder()
                .id(1L).uuid(SERVICE_UUID)
                .name("Netflix")
                .category(CategoryType.STREAMING)
                .priceProfile(BigDecimal.valueOf(45))
                .priceFull(BigDecimal.valueOf(150))
                .durationDays(30)
                .maxProfiles(5)
                .vendorId(vendorId)
                .isActive(true)
                .build();
    }

    private Vendor buildVendor(UUID uuid, Long id) {
        return Vendor.builder()
                .id(id).uuid(uuid)
                .userId(USER_ID).storeName("Mi Tienda").build();
    }
}
