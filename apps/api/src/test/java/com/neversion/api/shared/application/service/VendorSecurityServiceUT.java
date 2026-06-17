package com.neversion.api.shared.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorSecurityService Unit Tests (tech-debt A2)")
class VendorSecurityServiceUT {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    @InjectMocks private VendorSecurityService vendorSecurityService;

    private static final String EXTERNAL_ID = "auth|vendor-security";
    private static final Long USER_ID = 5L;
    private static final Long VENDOR_ID = 10L;

    private User user() {
        return User.builder().id(USER_ID).externalId(EXTERNAL_ID).role(UserRole.VENDOR).build();
    }

    private Vendor vendor() {
        return Vendor.builder().id(VENDOR_ID).userId(USER_ID).storeName("Vendor").build();
    }

    @Test
    @DisplayName("resolveCallerVendor - returns the vendor of the authenticated caller")
    void resolveCallerVendor_shouldReturnVendor() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor()));

        Vendor result = vendorSecurityService.resolveCallerVendor(EXTERNAL_ID);

        assertThat(result.getId()).isEqualTo(VENDOR_ID);
    }

    @Test
    @DisplayName("resolveVendorId - returns the vendor id of the authenticated caller")
    void resolveVendorId_shouldReturnVendorId() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor()));

        assertThat(vendorSecurityService.resolveVendorId(EXTERNAL_ID)).isEqualTo(VENDOR_ID);
    }

    @Test
    @DisplayName("resolveVendorId - throws when user is not found")
    void resolveVendorId_userNotFound_shouldThrow() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vendorSecurityService.resolveVendorId(EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("resolveVendorId - throws when vendor is not found for the user")
    void resolveVendorId_vendorNotFound_shouldThrow() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user()));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vendorSecurityService.resolveVendorId(EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor not found");
    }

    @Test
    @DisplayName("assertOwnership - passes when caller owns the resource")
    void assertOwnership_sameVendor_shouldPass() {
        assertThatCode(() -> vendorSecurityService.assertOwnership(VENDOR_ID, VENDOR_ID, "vendor 10"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("assertOwnership - throws AccessDeniedException when caller does not own the resource")
    void assertOwnership_differentVendor_shouldThrow() {
        assertThatThrownBy(() -> vendorSecurityService.assertOwnership(VENDOR_ID, 99L, "vendor 99"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("vendor 99");
    }

    @Test
    @DisplayName("assertOwnership - throws when caller vendor id is null")
    void assertOwnership_nullCaller_shouldThrow() {
        assertThatThrownBy(() -> vendorSecurityService.assertOwnership(null, VENDOR_ID, "vendor 10"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
