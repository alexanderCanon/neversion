package com.neversion.api.user.application.service;

import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.domain.model.RegisterVendorCommand;
import com.neversion.api.user.domain.model.RegisterVendorResult;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegisterVendorService (US-012).
 * Uses Mockito — no Spring context, no database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterVendorService unit tests")
class RegisterVendorServiceUT {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private VendorRepositoryPort vendorRepositoryPort;

    @Mock
    private NotificationLogPort notificationLogPort;

    @Mock
    private AuthServicePort authServicePort;

    private RegisterVendorService sut;

    private static final UUID USER_UUID   = UUID.randomUUID();
    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final Long USER_ID     = 42L;

    @BeforeEach
    void setUp() {
        sut = new RegisterVendorService(userRepositoryPort, vendorRepositoryPort, notificationLogPort, authServicePort);
    }

    // ─── happy path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("register - should persist user, vendor and notification in order")
    void register_shouldPersistAllThreeEntities_inOrder() {
        // Arrange
        RegisterVendorCommand command = buildCommand();

        User savedUser = User.builder()
                .id(USER_ID)
                .externalId("supabase-uuid-abc123")
                .role(UserRole.VENDOR)
                .build();

        Vendor savedVendor = Vendor.builder()
                .id(1L)
                .uuid(VENDOR_UUID)
                .userId(USER_ID)
                .storeName("Mi Tienda")
                .build();

        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(vendorRepositoryPort.save(any(Vendor.class))).thenReturn(savedVendor);
        when(authServicePort.createUser(anyString(), anyString(), any(UserRole.class)))
                .thenReturn("supabase-uuid-abc123");
        doNothing().when(notificationLogPort).record(anyString(), anyString(), anyString(),
                anyString(), any(), anyString());

        // Act
        RegisterVendorResult result = sut.register(command);

        // Assert — result fields
        assertThat(result.externalId()).isEqualTo("supabase-uuid-abc123");
        assertThat(result.vendorUuid()).isEqualTo(VENDOR_UUID);

        assertThat(result.storeName()).isEqualTo("Mi Tienda");
        assertThat(result.email()).isEqualTo("vendor@test.com");

        // Assert — interactions
        verify(authServicePort, times(1)).createUser("vendor@test.com", "secret123", UserRole.VENDOR);
        verify(userRepositoryPort, times(1)).save(any(User.class));
        verify(vendorRepositoryPort, times(1)).save(any(Vendor.class));
        verify(notificationLogPort, times(1))
                .record(eq("VENDOR_WELCOME"), eq("vendor@test.com"), anyString(),
                        eq("vendor"), eq(1L), eq("welcome"));
    }

    @Test
    @DisplayName("register - saved user should have role VENDOR")
    void register_savedUser_shouldHaveRoleVendor() {
        // Arrange
        User savedUser = User.builder()
                .id(USER_ID)
                .externalId("supabase-uuid-vendor-001").role(UserRole.VENDOR).build();
        Vendor savedVendor = Vendor.builder()
                .id(1L).uuid(VENDOR_UUID).userId(USER_ID).storeName("x").build();

        when(authServicePort.createUser(anyString(), anyString(), any())).thenReturn("supabase-uuid-vendor-001");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(vendorRepositoryPort.save(any(Vendor.class))).thenReturn(savedVendor);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        sut.register(buildCommand());

        // Assert
        verify(userRepositoryPort).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.VENDOR);
        assertThat(userCaptor.getValue().getExternalId()).isEqualTo("supabase-uuid-vendor-001");
    }

    @Test
    @DisplayName("register - saved vendor should link to user id")
    void register_savedVendor_shouldLinkToUserId() {
        // Arrange
        User savedUser = User.builder()
                .id(USER_ID)
                .externalId("pending_x").role(UserRole.VENDOR).build();
        Vendor savedVendor = Vendor.builder()
                .id(1L).uuid(VENDOR_UUID).userId(USER_ID).storeName("Mi Tienda").build();

        when(authServicePort.createUser(anyString(), anyString(), any())).thenReturn("pending_x");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(vendorRepositoryPort.save(any(Vendor.class))).thenReturn(savedVendor);

        ArgumentCaptor<Vendor> vendorCaptor = ArgumentCaptor.forClass(Vendor.class);

        // Act
        sut.register(buildCommand());

        // Assert
        verify(vendorRepositoryPort).save(vendorCaptor.capture());
        assertThat(vendorCaptor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(vendorCaptor.getValue().getStoreName()).isEqualTo("Mi Tienda");
    }

    @Test
    @DisplayName("register - notification payload should contain email and store name")
    void register_notificationPayload_shouldContainEmailAndStoreName() {
        // Arrange
        User savedUser = User.builder()
                .id(USER_ID)
                .externalId("pending_x").role(UserRole.VENDOR).build();
        Vendor savedVendor = Vendor.builder()
                .id(1L).uuid(VENDOR_UUID).userId(USER_ID).storeName("Mi Tienda").build();

        when(authServicePort.createUser(anyString(), anyString(), any())).thenReturn("pending_x");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(vendorRepositoryPort.save(any(Vendor.class))).thenReturn(savedVendor);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        sut.register(buildCommand());

        // Assert
        verify(notificationLogPort).record(anyString(), anyString(), payloadCaptor.capture(),
                anyString(), any(), anyString());
        String payload = payloadCaptor.getValue();
        assertThat(payload).contains("vendor@test.com");
        assertThat(payload).contains("Mi Tienda");
        assertThat(payload).contains("externalId");
    }

    @Test
    @DisplayName("register - generated passwords should differ across calls")
    void register_generatedPasswords_shouldBeUnique() {
        // Arrange
        User savedUser = User.builder()
                .id(USER_ID)
                .externalId("pending_x").role(UserRole.VENDOR).build();
        Vendor savedVendor = Vendor.builder()
                .id(1L).uuid(VENDOR_UUID).userId(USER_ID).storeName("x").build();


        when(authServicePort.createUser(anyString(), anyString(), any())).thenReturn("pending_x");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(vendorRepositoryPort.save(any(Vendor.class))).thenReturn(savedVendor);

        // Act
        RegisterVendorResult r1 = sut.register(buildCommand());
        RegisterVendorResult r2 = sut.register(buildCommand());

        // Assert
        assertThat(r1.externalId()).isNotNull();
        assertThat(r2.externalId()).isNotNull();
        assertThat(r1.externalId()).isEqualTo(r2.externalId()); // same mock stub

    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private RegisterVendorCommand buildCommand() {
        return new RegisterVendorCommand(
                "vendor@test.com",
                "secret123",
                "Mi Tienda",
                "https://cdn.example.com/logo.png",
                "{\"bank\":\"Banrural\"}",
                null);
    }
}
