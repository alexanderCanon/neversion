package com.neversion.api.dashboard.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionResult;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetExpiringSubscriptionsKpiService — EPIC-10 / US-063")
class GetExpiringSubscriptionsKpiServiceUT {

    private static final String EXTERNAL_ID = "auth|vendor-test";
    private static final Long USER_ID = 10L;
    private static final Long VENDOR_ID = 20L;
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-30T12:00:00Z"),
            ZoneOffset.UTC);

    @Mock private DashboardQueryPort dashboardQueryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    @Test
    @DisplayName("getForAuthenticatedVendor - should group active and suspended subscriptions by due date")
    void getForAuthenticatedVendor_validVendor_shouldGroupByDueDate() {
        GetExpiringSubscriptionsKpiService service = buildService();
        mockVendorResolution();

        ExpiringSubscriptionResult today = row(TODAY, "ACTIVE");
        ExpiringSubscriptionResult tomorrow = row(TODAY.plusDays(1), "SUSPENDED");
        ExpiringSubscriptionResult week = row(TODAY.plusDays(4), "ACTIVE");

        when(dashboardQueryPort.findExpiringSubscriptions(VENDOR_ID, TODAY, TODAY.plusDays(6)))
                .thenReturn(List.of(today, tomorrow, week));

        var result = service.getForAuthenticatedVendor(EXTERNAL_ID);

        assertThat(result.today()).containsExactly(today);
        assertThat(result.tomorrow()).containsExactly(tomorrow);
        assertThat(result.thisWeek()).containsExactly(week);
    }

    @Test
    @DisplayName("getForAuthenticatedVendor - should throw ResourceNotFoundException when vendor is missing")
    void getForAuthenticatedVendor_missingVendor_shouldThrow404() {
        GetExpiringSubscriptionsKpiService service = buildService();
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

    private GetExpiringSubscriptionsKpiService buildService() {
        return new GetExpiringSubscriptionsKpiService(
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

    private ExpiringSubscriptionResult row(LocalDate dueDate, String status) {
        return new ExpiringSubscriptionResult(
                UUID.randomUUID(),
                "Cliente",
                "Netflix",
                "Perfil 1",
                dueDate,
                status);
    }
}
