package com.neversion.api.dashboard.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
@DisplayName("GetActiveClientsKpiService — EPIC-10 / US-065")
class GetActiveClientsKpiServiceUT {

    private static final String EXTERNAL_ID = "auth|vendor-test";
    private static final Long USER_ID = 10L;
    private static final Long VENDOR_ID = 20L;

    @Mock private DashboardQueryPort dashboardQueryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    @Test
    @DisplayName("getForAuthenticatedVendor - should return unique active client count")
    void getForAuthenticatedVendor_validVendor_shouldReturnActiveClientCount() {
        GetActiveClientsKpiService service = buildService();
        mockVendorResolution();
        when(dashboardQueryPort.countActiveClients(VENDOR_ID)).thenReturn(7L);

        var result = service.getForAuthenticatedVendor(EXTERNAL_ID);

        assertThat(result.activeClientsCount()).isEqualTo(7L);
    }

    @Test
    @DisplayName("getForAuthenticatedVendor - should throw ResourceNotFoundException when vendor is missing")
    void getForAuthenticatedVendor_missingVendor_shouldThrow404() {
        GetActiveClientsKpiService service = buildService();
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

    private GetActiveClientsKpiService buildService() {
        return new GetActiveClientsKpiService(
                dashboardQueryPort,
                userRepositoryPort,
                vendorRepositoryPort);
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
