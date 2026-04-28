package com.neversion.api.client.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.client.application.port.in.ClientUseCase.ActiveSubscriptionSummary;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientDetail;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Unit tests for ClientService — EPIC-04 (US-029..032).
 * Convention: method_scenario_expected
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService — EPIC-04 unit tests")
class ClientServiceUT {

    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;

    private ClientService clientService;

    private static final UUID CLIENT_UUID  = UUID.randomUUID();
    private static final UUID VENDOR_UUID  = UUID.randomUUID();
    private static final String EXTERNAL_ID = "auth|vendor-test";
    private static final Long VENDOR_INTERNAL_ID = 10L;
    private static final Long CLIENT_INTERNAL_ID = 1L;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(
                clientRepositoryPort, userRepositoryPort, vendorRepositoryPort,
                subscriptionRepositoryPort, orderRepositoryPort, notificationLogPort);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Client buildClient() {
        return Client.builder()
                .id(CLIENT_INTERNAL_ID)
                .uuid(CLIENT_UUID)
                .vendorId(VENDOR_INTERNAL_ID)
                .name("Juan Pérez")
                .email("juan@gmail.com")
                .phone("55551234")
                .notes("Regular customer")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User buildUser() {
        return User.builder()
                .id(5L)
                .externalId(EXTERNAL_ID)
                .role(UserRole.VENDOR)
                .build();
    }

    private Vendor buildVendor() {
        return Vendor.builder()
                .id(VENDOR_INTERNAL_ID)
                .uuid(VENDOR_UUID)
                .userId(5L)
                .storeName("Test Vendor")
                .build();
    }

    private void mockOwnershipResolution() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID))
                .thenReturn(Optional.of(buildUser()));
        when(vendorRepositoryPort.findByUserId(5L))
                .thenReturn(Optional.of(buildVendor()));
    }

    // ── US-029: listByVendor ──────────────────────────────────────────────

    @Nested
    @DisplayName("listByVendor (US-029)")
    class ListByVendor {

        @Test
        @DisplayName("should return clients of the vendor with no filters")
        void listByVendor_noFilters_shouldReturnVendorClients() {
            // Given
            mockOwnershipResolution();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(buildVendor()));
            when(clientRepositoryPort.findByVendorId(VENDOR_INTERNAL_ID, null, null, null))
                    .thenReturn(List.of(buildClient()));

            // When
            List<Client> result = clientService.listByVendor(VENDOR_UUID, null, null, null, EXTERNAL_ID);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVendorId()).isEqualTo(VENDOR_INTERNAL_ID);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own vendor")
        void listByVendor_wrongVendor_shouldThrow403() {
            // Given
            UUID otherVendorUuid = UUID.randomUUID();
            Vendor otherVendor = Vendor.builder().id(99L).uuid(otherVendorUuid).userId(5L).build();
            when(vendorRepositoryPort.findByUuid(otherVendorUuid)).thenReturn(Optional.of(otherVendor));
            when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(buildUser()));
            when(vendorRepositoryPort.findByUserId(5L)).thenReturn(Optional.of(buildVendor()));

            // When / Then
            assertThatThrownBy(() -> clientService.listByVendor(
                    otherVendorUuid, null, null, null, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when vendor not found")
        void listByVendor_vendorNotFound_shouldThrow404() {
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.listByVendor(
                    VENDOR_UUID, null, null, null, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── US-030: getDetail ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getDetail (US-030)")
    class GetDetail {

        @Test
        @DisplayName("should return detail with active subscriptions and order history")
        void getDetail_ownedClient_shouldReturnDetail() {
            // Given
            mockOwnershipResolution();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));

            Subscription activeSub = Subscription.builder()
                    .id(1L).uuid(UUID.randomUUID()).clientId(CLIENT_INTERNAL_ID)
                    .status(SubStatus.ACTIVE).paymentDueDate(LocalDate.now().plusDays(10))
                    .build();
            when(subscriptionRepositoryPort.findByClientId(CLIENT_INTERNAL_ID))
                    .thenReturn(List.of(activeSub));

            Order order = Order.builder()
                    .id(1L).uuid(UUID.randomUUID()).status(OrderStatus.COMPLETED).build();
            when(orderRepositoryPort.findByClientId(CLIENT_INTERNAL_ID))
                    .thenReturn(List.of(order));

            // When
            ClientDetail detail = clientService.getDetail(CLIENT_UUID, EXTERNAL_ID);

            // Then
            assertThat(detail.client().getUuid()).isEqualTo(CLIENT_UUID);
            assertThat(detail.activeSubscriptions()).hasSize(1);
            assertThat(detail.activeSubscriptions().get(0).status()).isEqualTo("ACTIVE");
            assertThat(detail.orderHistory()).hasSize(1);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when client belongs to another vendor")
        void getDetail_notOwned_shouldThrow403() {
            // Given
            Client anotherVendorClient = Client.builder()
                    .id(CLIENT_INTERNAL_ID).uuid(CLIENT_UUID)
                    .vendorId(99L) // different vendor
                    .name("Other")
                    .build();
            when(clientRepositoryPort.findById(CLIENT_UUID))
                    .thenReturn(Optional.of(anotherVendorClient));
            mockOwnershipResolution();

            // When / Then
            assertThatThrownBy(() -> clientService.getDetail(CLIENT_UUID, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    // ── US-031: createForVendor ───────────────────────────────────────────

    @Nested
    @DisplayName("createForVendor (US-031)")
    class CreateForVendor {

        @Test
        @DisplayName("should create client linked to caller's vendor")
        void createForVendor_validRequest_shouldCreate() {
            // Given
            mockOwnershipResolution();
            Client newClient = Client.builder().name("Ana López").email("ana@test.com").build();
            Client savedClient = Client.builder()
                    .id(2L).uuid(UUID.randomUUID()).vendorId(VENDOR_INTERNAL_ID)
                    .name("Ana López").email("ana@test.com").build();

            when(clientRepositoryPort.findByEmail("ana@test.com")).thenReturn(Optional.empty());
            when(clientRepositoryPort.save(any(Client.class))).thenReturn(savedClient);

            // When
            Client result = clientService.createForVendor(newClient, EXTERNAL_ID);

            // Then
            assertThat(result.getVendorId()).isEqualTo(VENDOR_INTERNAL_ID);
            assertThat(result.getEmail()).isEqualTo("ana@test.com");
            verify(notificationLogPort).record(eq("CLIENT_WELCOME"), eq("ana@test.com"), anyString());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when email already exists")
        void createForVendor_duplicateEmail_shouldThrow400() {
            // Given
            Client existing = buildClient();
            when(clientRepositoryPort.findByEmail("juan@gmail.com"))
                    .thenReturn(Optional.of(existing));

            Client newClient = Client.builder().name("Otro").email("juan@gmail.com").build();

            // When / Then
            assertThatThrownBy(() -> clientService.createForVendor(newClient, EXTERNAL_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("juan@gmail.com");
        }
    }

    // ── US-032: update ────────────────────────────────────────────────────

    @Nested
    @DisplayName("update (US-032)")
    class Update {

        @Test
        @DisplayName("should update name, phone, notes — not email")
        void update_ownedClient_shouldUpdateBasicFields() {
            // Given
            mockOwnershipResolution();
            Client existing = buildClient();
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(existing));
            when(clientRepositoryPort.save(existing)).thenReturn(existing);

            // When
            Client result = clientService.update(CLIENT_UUID, "Juan Nuevo", "99998888", "VIP",
                    EXTERNAL_ID);

            // Then
            assertThat(result.getName()).isEqualTo("Juan Nuevo");
            assertThat(result.getPhone()).isEqualTo("99998888");
            assertThat(result.getNotes()).isEqualTo("VIP");
            // email must NOT change
            assertThat(result.getEmail()).isEqualTo("juan@gmail.com");
            verify(clientRepositoryPort).save(existing);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when client belongs to another vendor")
        void update_notOwned_shouldThrow403() {
            // Given
            Client anotherVendorClient = Client.builder()
                    .id(CLIENT_INTERNAL_ID).uuid(CLIENT_UUID).vendorId(99L).name("Other").build();
            when(clientRepositoryPort.findById(CLIENT_UUID))
                    .thenReturn(Optional.of(anotherVendorClient));
            mockOwnershipResolution();

            // When / Then
            assertThatThrownBy(() -> clientService.update(
                    CLIENT_UUID, "Nuevo", null, null, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void update_notFound_shouldThrow404() {
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.update(
                    CLIENT_UUID, "Nuevo", null, null, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── Legacy: create, getById, delete ──────────────────────────────────

    @Nested
    @DisplayName("Legacy operations")
    class Legacy {

        @Test
        @DisplayName("create - should delegate save to repository")
        void create_shouldDelegateSaveToRepository() {
            Client client = buildClient();
            when(clientRepositoryPort.save(client)).thenReturn(client);
            Client result = clientService.create(client);
            assertThat(result).isNotNull();
            verify(clientRepositoryPort).save(client);
        }

        @Test
        @DisplayName("getById - should return client when found")
        void getById_shouldReturnClient_whenFound() {
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));
            Client result = clientService.getById(CLIENT_UUID);
            assertThat(result.getUuid()).isEqualTo(CLIENT_UUID);
        }

        @Test
        @DisplayName("getById - should throw ResourceNotFoundException when not found")
        void getById_shouldThrowResourceNotFound_whenNotFound() {
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> clientService.getById(CLIENT_UUID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("delete - should delete client when found")
        void delete_shouldDeleteClient_whenFound() {
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(buildClient()));
            clientService.delete(CLIENT_UUID);
            verify(clientRepositoryPort).deleteById(CLIENT_UUID);
        }
    }
}
