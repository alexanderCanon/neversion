package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;


@ExtendWith(MockitoExtension.class)
@DisplayName("ListSubscriptionsService — US-043 unit tests")
class ListSubscriptionsServiceUT {

    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;

    private ListSubscriptionsService listSubscriptionsService;

    private static final UUID VENDOR_UUID = UUID.randomUUID();
    private static final UUID SERVICE_UUID = UUID.randomUUID();
    private static final String EXTERNAL_ID = "auth|vendor-subscriptions";
    private static final Long USER_ID = 5L;
    private static final Long VENDOR_ID = 10L;
    private static final Long SERVICE_ID = 20L;

    @BeforeEach
    void setUp() {
        listSubscriptionsService = new ListSubscriptionsService(
                subscriptionRepositoryPort,
                vendorRepositoryPort,
                serviceRepositoryPort,
                new VendorSecurityService(userRepositoryPort, vendorRepositoryPort));
    }

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .externalId(EXTERNAL_ID)
                .role(UserRole.VENDOR)
                .build();
    }

    private Vendor buildVendor(Long id, UUID uuid) {
        return Vendor.builder()
                .id(id)
                .uuid(uuid)
                .userId(USER_ID)
                .storeName("Vendor")
                .build();
    }

    private Subscription buildSubscription(SubStatus status, LocalDate dueDate) {
        return Subscription.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .vendorId(VENDOR_ID)
                .clientId(30L)
                .profileId(40L)
                .status(status)
                .paymentDueDate(dueDate)
                .build();
    }

    private void mockOwnershipResolution() {
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(buildUser()));
        when(vendorRepositoryPort.findByUserId(USER_ID))
                .thenReturn(Optional.of(buildVendor(VENDOR_ID, VENDOR_UUID)));
    }

    @Nested
    @DisplayName("listByVendor")
    class ListByVendor {

        @Test
        @DisplayName("should return vendor subscriptions ordered by repository query")
        void listByVendor_ownedVendor_shouldReturnSubscriptions() {
            Vendor vendor = buildVendor(VENDOR_ID, VENDOR_UUID);
            Subscription subscription = buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(3));
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            mockOwnershipResolution();
            when(subscriptionRepositoryPort.findByVendorIdFiltered(VENDOR_ID, null, null))
                    .thenReturn(List.of(subscription));

            List<Subscription> result = listSubscriptionsService.listByVendor(
                    VENDOR_UUID, null, null, EXTERNAL_ID);

            assertThat(result).containsExactly(subscription);
            verify(subscriptionRepositoryPort).findByVendorIdFiltered(VENDOR_ID, null, null);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own vendor")
        void listByVendor_wrongVendor_shouldThrow403() {
            UUID otherVendorUuid = UUID.randomUUID();
            Vendor otherVendor = buildVendor(99L, otherVendorUuid);
            when(vendorRepositoryPort.findByUuid(otherVendorUuid)).thenReturn(Optional.of(otherVendor));
            mockOwnershipResolution();

            assertThatThrownBy(() -> listSubscriptionsService.listByVendor(
                    otherVendorUuid, null, null, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should resolve service UUID filter before querying")
        void listByVendor_serviceFilter_shouldResolveServiceId() {
            Vendor vendor = buildVendor(VENDOR_ID, VENDOR_UUID);
            Service service = Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).name("Netflix").build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            mockOwnershipResolution();
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(subscriptionRepositoryPort.findByVendorIdFiltered(VENDOR_ID, SERVICE_ID, SubStatus.SUSPENDED))
                    .thenReturn(List.of());

            List<Subscription> result = listSubscriptionsService.listByVendor(
                    VENDOR_UUID, SERVICE_UUID, SubStatus.SUSPENDED, EXTERNAL_ID);

            assertThat(result).isEmpty();
            verify(subscriptionRepositoryPort)
                    .findByVendorIdFiltered(VENDOR_ID, SERVICE_ID, SubStatus.SUSPENDED);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when vendor does not exist")
        void listByVendor_missingVendor_shouldThrow404() {
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> listSubscriptionsService.listByVendor(
                    VENDOR_UUID, null, null, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listViewsByVendor (tech-debt A3 — single-query projection)")
    class ListViewsByVendor {

        private SubscriptionListView buildView() {
            return new SubscriptionListView(
                    UUID.randomUUID(), UUID.randomUUID(), "Profile 1",
                    UUID.randomUUID(), "Client", UUID.randomUUID(), "Netflix",
                    SubStatus.ACTIVE, LocalDate.now(), null, LocalDate.now().plusDays(3),
                    1L, null, null);
        }

        @Test
        @DisplayName("should delegate to the projection query when caller owns vendor")
        void listViewsByVendor_ownedVendor_shouldReturnViews() {
            Vendor vendor = buildVendor(VENDOR_ID, VENDOR_UUID);
            SubscriptionListView view = buildView();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            mockOwnershipResolution();
            when(subscriptionRepositoryPort.findVendorSubscriptionViews(VENDOR_ID, null, null))
                    .thenReturn(List.of(view));

            List<SubscriptionListView> result = listSubscriptionsService.listViewsByVendor(
                    VENDOR_UUID, null, null, EXTERNAL_ID);

            assertThat(result).containsExactly(view);
            verify(subscriptionRepositoryPort).findVendorSubscriptionViews(VENDOR_ID, null, null);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own vendor")
        void listViewsByVendor_wrongVendor_shouldThrow403() {
            UUID otherVendorUuid = UUID.randomUUID();
            Vendor otherVendor = buildVendor(99L, otherVendorUuid);
            when(vendorRepositoryPort.findByUuid(otherVendorUuid)).thenReturn(Optional.of(otherVendor));
            mockOwnershipResolution();

            assertThatThrownBy(() -> listSubscriptionsService.listViewsByVendor(
                    otherVendorUuid, null, null, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should resolve service UUID filter before querying the projection")
        void listViewsByVendor_serviceFilter_shouldResolveServiceId() {
            Vendor vendor = buildVendor(VENDOR_ID, VENDOR_UUID);
            Service service = Service.builder().id(SERVICE_ID).uuid(SERVICE_UUID).name("Netflix").build();
            when(vendorRepositoryPort.findByUuid(VENDOR_UUID)).thenReturn(Optional.of(vendor));
            mockOwnershipResolution();
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(subscriptionRepositoryPort.findVendorSubscriptionViews(VENDOR_ID, SERVICE_ID, SubStatus.ACTIVE))
                    .thenReturn(List.of());

            List<SubscriptionListView> result = listSubscriptionsService.listViewsByVendor(
                    VENDOR_UUID, SERVICE_UUID, SubStatus.ACTIVE, EXTERNAL_ID);

            assertThat(result).isEmpty();
            verify(subscriptionRepositoryPort)
                    .findVendorSubscriptionViews(VENDOR_ID, SERVICE_ID, SubStatus.ACTIVE);
        }
    }
}
