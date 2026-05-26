package com.neversion.api.user.application.service;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.SupabaseAuthPort;
import com.neversion.api.user.domain.model.RegisterClientCommand;
import com.neversion.api.user.domain.model.RegisterClientResult;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegisterClientService (US-013).
 * Uses Mockito — no Spring context, no database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterClientService unit tests")
class RegisterClientServiceUT {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private ClientRepositoryPort clientRepositoryPort;

    @Mock
    private VendorRepositoryPort vendorRepositoryPort;

    @Mock
    private NotificationLogPort notificationLogPort;

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    private RegisterClientService sut;

    private static final UUID USER_UUID   = UUID.randomUUID();
    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final Long USER_ID     = 42L;
    private static final Long VENDOR_ID   = 10L;

    @BeforeEach
    void setUp() {
        sut = new RegisterClientService(
                userRepositoryPort, clientRepositoryPort,
                vendorRepositoryPort, notificationLogPort, supabaseAuthPort);
    }

    // ─── happy path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("register - should persist user, client and notification in order")
    void register_shouldPersistAllThreeEntities_inOrder() {
        // Arrange
        RegisterClientCommand command = buildCommand();
        stubHappyPath();

        // Act
        RegisterClientResult result = sut.register(command);

        // Assert — result fields
        assertThat(result.userUuid()).isEqualTo(USER_UUID);
        assertThat(result.clientUuid()).isEqualTo(CLIENT_UUID);
        assertThat(result.name()).isEqualTo("Juan Pérez");
        assertThat(result.email()).isEqualTo("cliente@correo.com");

        // Assert — interactions
        verify(vendorRepositoryPort, times(1)).findByUuid(VENDOR_UUID);
        verify(supabaseAuthPort, times(1)).createUser("cliente@correo.com", "secret123", UserRole.CLIENT);
        verify(userRepositoryPort, times(1)).save(any(User.class));
        verify(clientRepositoryPort, times(1)).save(any(Client.class));
        verify(notificationLogPort, times(1))
                .record(eq("CLIENT_REGISTRATION"), eq("cliente@correo.com"), anyString(),
                        eq("client"), eq(1L), eq("welcome"));
    }

    @Test
    @DisplayName("register - saved user should have role CLIENT")
    void register_savedUser_shouldHaveRoleClient() {
        stubHappyPath();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        sut.register(buildCommand());

        verify(userRepositoryPort).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.CLIENT);
        assertThat(userCaptor.getValue().getExternalId()).isEqualTo("supabase-uuid-abc123");
    }

    @Test
    @DisplayName("register - saved client should link to user and vendor")
    void register_savedClient_shouldLinkToUserAndVendor() {
        stubHappyPath();
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);

        sut.register(buildCommand());

        verify(clientRepositoryPort).save(clientCaptor.capture());
        Client captured = clientCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(USER_ID);
        assertThat(captured.getVendorId()).isEqualTo(VENDOR_ID);
        assertThat(captured.getName()).isEqualTo("Juan Pérez");
        assertThat(captured.getEmail()).isEqualTo("cliente@correo.com");
        assertThat(captured.getPhone()).isEqualTo("50255551234");
    }

    @Test
    @DisplayName("register - should link existing manual client by vendor phone and update name")
    void register_existingManualClient_shouldLinkAndUpdateName() {
        // Arrange
        RegisterClientCommand command = buildCommand();
        Vendor vendor = Vendor.builder()
                .id(VENDOR_ID).uuid(VENDOR_UUID)
                .userId(99L).storeName("Mi Tienda").build();
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
        Client manualClient = Client.builder()
                .id(1L).uuid(CLIENT_UUID).vendorId(VENDOR_ID)
                .name("Nombre Viejo").phone("50255551234").build();
        when(clientRepositoryPort.findByVendorIdAndPhone(VENDOR_ID, "50255551234"))
                .thenReturn(Optional.of(manualClient));
        User savedUser = User.builder()
                .id(USER_ID).uuid(USER_UUID)
                .externalId("supabase-uuid-abc123").role(UserRole.CLIENT).build();
        when(supabaseAuthPort.createUser("cliente@correo.com", "secret123", UserRole.CLIENT))
                .thenReturn("supabase-uuid-abc123");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(clientRepositoryPort.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterClientResult result = sut.register(command);

        // Assert
        assertThat(result.clientUuid()).isEqualTo(CLIENT_UUID);
        assertThat(result.name()).isEqualTo("Juan Pérez");
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepositoryPort).save(clientCaptor.capture());
        Client linkedClient = clientCaptor.getValue();
        assertThat(linkedClient.getId()).isEqualTo(1L);
        assertThat(linkedClient.getUserId()).isEqualTo(USER_ID);
        assertThat(linkedClient.getName()).isEqualTo("Juan Pérez");
        assertThat(linkedClient.getEmail()).isEqualTo("cliente@correo.com");
        verify(clientRepositoryPort, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("register - should reject phone already linked to authenticated client")
    void register_existingLinkedClient_shouldThrow() {
        RegisterClientCommand command = buildCommand();
        Vendor vendor = Vendor.builder()
                .id(VENDOR_ID).uuid(VENDOR_UUID)
                .userId(99L).storeName("Mi Tienda").build();
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
        Client linkedClient = Client.builder()
                .id(1L).uuid(CLIENT_UUID).vendorId(VENDOR_ID).userId(77L)
                .name("Juan").phone("50255551234").build();
        when(clientRepositoryPort.findByVendorIdAndPhone(VENDOR_ID, "50255551234"))
                .thenReturn(Optional.of(linkedClient));

        assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phone already linked");

        verifyNoInteractions(supabaseAuthPort);
        verifyNoInteractions(userRepositoryPort);
        verify(notificationLogPort, never()).record(anyString(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("register - notification payload should contain client data")
    void register_notificationPayload_shouldContainClientData() {
        stubHappyPath();
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        sut.register(buildCommand());

        verify(notificationLogPort).record(anyString(), anyString(), payloadCaptor.capture(),
                anyString(), any(), anyString());
        String payload = payloadCaptor.getValue();
        assertThat(payload).contains("cliente@correo.com");
        assertThat(payload).contains("Juan Pérez");
        assertThat(payload).contains("externalId");
    }

    // ─── error case ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("register - should throw ResourceNotFoundException when vendor not found")
    void register_shouldThrow_whenVendorNotFound() {
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.register(buildCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(VENDOR_UUID.toString());

        // No user or client should be persisted
        verifyNoInteractions(userRepositoryPort);
        verifyNoInteractions(clientRepositoryPort);
        verifyNoInteractions(notificationLogPort);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private RegisterClientCommand buildCommand() {
        return new RegisterClientCommand(
                "cliente@correo.com",
                "secret123",
                "Juan Pérez",
                "+502 5555-1234",
                VENDOR_UUID);
    }

    private void stubHappyPath() {
        Vendor vendor = Vendor.builder()
                .id(VENDOR_ID).uuid(VENDOR_UUID)
                .userId(99L).storeName("Mi Tienda").build();
        when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));

        User savedUser = User.builder()
                .id(USER_ID).uuid(USER_UUID)
                .externalId("supabase-uuid-abc123").role(UserRole.CLIENT).build();
        when(supabaseAuthPort.createUser(anyString(), anyString(), any(UserRole.class)))
                .thenReturn("supabase-uuid-abc123");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        Client savedClient = Client.builder()
                .id(1L).uuid(CLIENT_UUID)
                .userId(USER_ID).vendorId(VENDOR_ID)
                .name("Juan Pérez").email("cliente@correo.com").phone("50255551234").build();
        when(clientRepositoryPort.save(any(Client.class))).thenReturn(savedClient);
    }
}
