package com.neversion.api.dashboard.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSuccessfulRenewalsKpiService — EPIC-10 / US-066")
class GetSuccessfulRenewalsKpiServiceUT {

    private static final String EXTERNAL_ID = "auth|vendor-test";
    private static final Long USER_ID = 10L;
    private static final Long VENDOR_ID = 20L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-30T12:00:00Z"),
            ZoneOffset.UTC);

    @Mock private DashboardQueryPort dashboardQueryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    @Test
    @DisplayName("getForAuthenticatedVendor - should return current month successful renewal count")
    void getForAuthenticatedVendor_validVendor_shouldReturnSuccessfulRenewalCount() {
        GetSuccessfulRenewalsKpiService service = buildService();
        mockVendorResolution();
        OffsetDateTime periodStart = OffsetDateTime.parse("2026-04-01T00:00:00Z");
        OffsetDateTime nextPeriodStart = OffsetDateTime.parse("2026-05-01T00:00:00Z");
        when(dashboardQueryPort.countSuccessfulRenewals(VENDOR_ID, periodStart, nextPeriodStart))
                .thenReturn(5L);

        var result = service.getForAuthenticatedVendor(EXTERNAL_ID);

        assertThat(result.successfulRenewalsCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getForAuthenticatedVendor - should throw ResourceNotFoundException when vendor is missing")
    void getForAuthenticatedVendor_missingVendor_shouldThrow404() {
        GetSuccessfulRenewalsKpiService service = buildService();
        User user = User.builder()
                .id(USER_ID)
                .externalId(EXTERNAL_ID)
                .role(UserRole.VENDOR)
                .build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForAuthenticatedVendor(EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private GetSuccessfulRenewalsKpiService buildService() {
        return new GetSuccessfulRenewalsKpiService(
                dashboardQueryPort,
                userRepositoryPort,
                vendorRepositoryPort,
                FIXED_CLOCK);
    }

    private void mockVendorResolution() {
        User user = User.builder()
                .id(USER_ID)
                .externalId(EXTERNAL_ID)
                .role(UserRole.VENDOR)
                .build();
        Vendor vendor = Vendor.builder()
                .id(VENDOR_ID)
                .uuid(UUID.randomUUID())
                .userId(USER_ID)
                .storeName("Vendor")
                .build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }
}
