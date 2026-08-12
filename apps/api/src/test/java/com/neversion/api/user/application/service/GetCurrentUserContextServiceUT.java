package com.neversion.api.user.application.service;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.model.CurrentUserContextResult;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCurrentUserContextService unit tests")
class GetCurrentUserContextServiceUT {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private VendorRepositoryPort vendorRepositoryPort;

    private GetCurrentUserContextService service;

    private static final String EXTERNAL_ID = "supabase-vendor";
    private static final Long USER_ID = 1L;
    private static final UUID USER_UUID = UUID.randomUUID();
    private static final UUID VENDOR_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetCurrentUserContextService(userRepositoryPort, vendorRepositoryPort);
    }

    @Test
    @DisplayName("get - should return vendor context for vendor user")
    void get_shouldReturnVendorContext_whenVendorUser() {
        User user = buildUser(UserRole.VENDOR);
        Vendor vendor = Vendor.builder()
                .id(10L)
                .uuid(VENDOR_UUID)
                .userId(USER_ID)
                .storeName("Mi Tienda")
                .build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));

        CurrentUserContextResult result = service.get(EXTERNAL_ID);

        assertThat(result.externalId()).isEqualTo(EXTERNAL_ID);
        assertThat(result.role()).isEqualTo(UserRole.VENDOR);

        assertThat(result.vendorUuid()).isEqualTo(VENDOR_UUID);
        assertThat(result.storeName()).isEqualTo("Mi Tienda");
    }

    @Test
    @DisplayName("get - should return user context without vendor for super admin")
    void get_shouldReturnUserContextOnly_whenSuperAdmin() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID))
                .thenReturn(Optional.of(buildUser(UserRole.SUPER_ADMIN)));

        CurrentUserContextResult result = service.get(EXTERNAL_ID);

        assertThat(result.role()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(result.vendorUuid()).isNull();
        assertThat(result.storeName()).isNull();
    }

    @Test
    @DisplayName("get - should throw when internal user is missing")
    void get_shouldThrow_whenUserMissing() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User buildUser(UserRole role) {
        return User.builder()
                .id(USER_ID)
                .externalId(EXTERNAL_ID)
                .role(role)
                .build();
    }
}

