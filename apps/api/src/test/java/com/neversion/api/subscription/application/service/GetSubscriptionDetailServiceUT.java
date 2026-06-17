package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase.SubscriptionDetail;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSubscriptionDetailService — US-044 unit tests")
class GetSubscriptionDetailServiceUT {

    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    private GetSubscriptionDetailService getSubscriptionDetailService;

    private static final UUID SUBSCRIPTION_UUID = UUID.randomUUID();
    private static final String EXTERNAL_ID = "auth|subscription-detail";
    private static final Long USER_ID = 5L;
    private static final Long VENDOR_ID = 10L;
    private static final Long CLIENT_ID = 20L;
    private static final Long PROFILE_ID = 30L;
    private static final Long ACCOUNT_ID = 40L;
    private static final Long SERVICE_ID = 50L;
    private static final Long ORDER_ID = 60L;

    @BeforeEach
    void setUp() {
        getSubscriptionDetailService = new GetSubscriptionDetailService(
                subscriptionRepositoryPort,
                clientRepositoryPort,
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                orderRepositoryPort,
                new VendorSecurityService(userRepositoryPort, vendorRepositoryPort));
    }

    private void mockOwnershipResolution(Long vendorId) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).role(UserRole.VENDOR).build();
        Vendor vendor = Vendor.builder().id(vendorId).userId(USER_ID).storeName("Vendor").build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Subscription buildSubscription(Long vendorId) {
        return Subscription.builder()
                .id(1L)
                .uuid(SUBSCRIPTION_UUID)
                .vendorId(vendorId)
                .clientId(CLIENT_ID)
                .profileId(PROFILE_ID)
                .orderId(ORDER_ID)
                .serviceId(SERVICE_ID)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .paymentDueDate(LocalDate.now().plusDays(30))
                .monthsPaid(1L)
                .priceSold(new BigDecimal("100.00"))
                .discountApplied(new BigDecimal("10.00"))
                .saleMode(SaleMode.BY_PROFILE)
                .status(SubStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("getDetail")
    class GetDetail {

        @Test
        @DisplayName("should return subscription detail when caller owns subscription")
        void getDetail_ownedSubscription_shouldReturnDetail() {
            Subscription subscription = buildSubscription(VENDOR_ID);
            Client client = Client.builder().id(CLIENT_ID).uuid(UUID.randomUUID()).name("Juan").build();
            Profile profile = Profile.builder()
                    .id(PROFILE_ID).uuid(UUID.randomUUID()).accountId(ACCOUNT_ID)
                    .name("Profile 1").pin("1234").status(ProfileStatus.ACTIVE).build();
            Account account = Account.builder()
                    .id(ACCOUNT_ID).uuid(UUID.randomUUID()).serviceId(SERVICE_ID)
                    .email("account@test.com").plan("Premium").saleMode(SaleMode.BY_PROFILE)
                    .status(AccountStatus.FULL).build();
            Service service = Service.builder().id(SERVICE_ID).uuid(UUID.randomUUID()).name("Netflix").build();
            Order order = Order.builder()
                    .id(ORDER_ID).uuid(UUID.randomUUID()).status(OrderStatus.COMPLETED)
                    .total(new BigDecimal("100.00")).discount(new BigDecimal("10.00"))
                    .approvedAt(Instant.now()).build();

            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID)).thenReturn(Optional.of(subscription));
            mockOwnershipResolution(VENDOR_ID);
            when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(client));
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(profile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.of(service));
            when(orderRepositoryPort.findByInternalId(ORDER_ID)).thenReturn(Optional.of(order));

            SubscriptionDetail detail = getSubscriptionDetailService.getDetail(SUBSCRIPTION_UUID, EXTERNAL_ID);

            assertThat(detail.subscription().getUuid()).isEqualTo(SUBSCRIPTION_UUID);
            assertThat(detail.subscription().getPriceSold()).isEqualByComparingTo("100.00");
            assertThat(detail.client().getName()).isEqualTo("Juan");
            assertThat(detail.service().getName()).isEqualTo("Netflix");
            assertThat(detail.order().getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own subscription")
        void getDetail_notOwned_shouldThrow403() {
            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                    .thenReturn(Optional.of(buildSubscription(99L)));
            mockOwnershipResolution(VENDOR_ID);

            assertThatThrownBy(() -> getSubscriptionDetailService.getDetail(SUBSCRIPTION_UUID, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when subscription does not exist")
        void getDetail_missingSubscription_shouldThrow404() {
            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getSubscriptionDetailService.getDetail(SUBSCRIPTION_UUID, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
