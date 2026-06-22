package com.neversion.api.assignment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmAssignmentService Unit Tests")
class ConfirmAssignmentServiceUT {

    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private OrderStatusHistoryPort orderStatusHistoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private DeliverAccessUseCase deliverAccessUseCase;
    @Mock private AssignmentContextResolver contextResolver;
    @Mock private ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    private ConfirmAssignmentService service;
    private UUID orderUuid;
    private UUID profileUuid;
    private Order order;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
        service = new ConfirmAssignmentService(
                orderRepositoryPort,
                orderStatusHistoryPort,
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                clientRepositoryPort,
                subscriptionRepositoryPort,
                deliverAccessUseCase,
                contextResolver,
                historyRepositoryPort);

        orderUuid = UUID.randomUUID();
        profileUuid = UUID.randomUUID();
        vendor = Vendor.builder().id(7L).build();
        order = Order.builder()
                .id(11L)
                .uuid(orderUuid)
                .clientId(40L)
                .vendorId(7L)
                .status(OrderStatus.VALIDATED)
                .approvedAt(Instant.parse("2026-04-28T15:00:00Z"))
                .build();
    }

    @Test
    @DisplayName("confirm_shouldSetProfileActive_createSubscription_completeOrder")
    void confirm_shouldSetProfileActive_createSubscription_completeOrder() {
        givenHappyPath(30);
        UUID subscriptionUuid = UUID.randomUUID();
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription input = invocation.getArgument(0);
            input.setUuid(subscriptionUuid);
            return input;
        });

        AssignmentResult result = service.confirm(orderUuid, profileUuid, "caller");

        assertThat(result.subscriptionUuid()).isEqualTo(subscriptionUuid);
        assertThat(result.orderUuid()).isEqualTo(orderUuid);
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 4, 28));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 5, 28));

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepositoryPort).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getStatus()).isEqualTo(ProfileStatus.ACTIVE);

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepositoryPort).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue().getOrderId()).isEqualTo(11L);
        assertThat(subscriptionCaptor.getValue().getPaymentDueDate()).isEqualTo(LocalDate.of(2026, 5, 28));
        assertThat(subscriptionCaptor.getValue().getStatus()).isEqualTo(SubStatus.ACTIVE);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepositoryPort).save(order);
        verify(orderStatusHistoryPort).record(any(OrderStatusChange.class));
        verify(deliverAccessUseCase).deliver(any(Subscription.class));
    }

    @Test
    @DisplayName("confirm_shouldThrow400_whenProfileNotAvailable")
    void confirm_shouldThrow400_whenProfileNotAvailable() {
        givenValidatedOrder();
        when(profileRepositoryPort.findById(profileUuid)).thenReturn(Optional.of(Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .vendorId(7L)
                .status(ProfileStatus.BLOCKED)
                .build()));

        assertThatThrownBy(() -> service.confirm(orderUuid, profileUuid, "caller"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("AVAILABLE");
    }

    @Test
    @DisplayName("confirm_shouldThrow400_whenServiceHasNoDurationDays")
    void confirm_shouldThrow400_whenServiceHasNoDurationDays() {
        givenHappyPath(null);

        assertThatThrownBy(() -> service.confirm(orderUuid, profileUuid, "caller"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("duration");
    }

    @Test
    @DisplayName("confirm_shouldThrow409_whenAssignmentAlreadyConfirmed")
    void confirm_shouldThrow409_whenAssignmentAlreadyConfirmed() {
        givenValidatedOrder();
        when(subscriptionRepositoryPort.findByOrderId(11L)).thenReturn(Optional.of(Subscription.builder().build()));

        assertThatThrownBy(() -> service.confirm(orderUuid, profileUuid, "caller"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    @DisplayName("confirm_shouldNotRevertOrder_whenNotificationThrows")
    void confirm_shouldNotRevertOrder_whenNotificationThrows() {
        givenHappyPath(30);
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription input = invocation.getArgument(0);
            input.setUuid(UUID.randomUUID());
            return input;
        });
        doThrow(new RuntimeException("mail down")).when(deliverAccessUseCase).deliver(any(Subscription.class));

        AssignmentResult result = service.confirm(orderUuid, profileUuid, "caller");

        assertThat(result.notificationQueued()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepositoryPort).save(order);
    }

    @Test
    @DisplayName("confirm_fullAccount_shouldAnchorSubscriptionToOwnerProfile_andActivateAllProfiles")
    void confirm_fullAccount_shouldAnchorSubscriptionToOwnerProfile_andActivateAllProfiles() {
        givenValidatedOrder();
        Profile ownerProfile = Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .vendorId(7L)
                .accountId(20L)
                .isOwner(true)
                .status(ProfileStatus.AVAILABLE)
                .build();
        Profile secondaryProfile = Profile.builder()
                .id(51L)
                .uuid(UUID.randomUUID())
                .vendorId(7L)
                .accountId(20L)
                .isOwner(false)
                .status(ProfileStatus.AVAILABLE)
                .build();

        Account fullAccount = Account.builder()
                .id(20L)
                .uuid(UUID.randomUUID())
                .serviceId(30L)
                .saleMode(SaleMode.FULL_ACCOUNT)
                .status(AccountStatus.AVAILABLE)
                .build();

        when(profileRepositoryPort.findById(profileUuid)).thenReturn(Optional.of(ownerProfile));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(fullAccount));
        when(contextResolver.resolveSingleServiceForOrder(order)).thenReturn(Service.builder()
                .id(30L)
                .name("Netflix")
                .durationDays(30)
                .build());
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .name("Netflix")
                .durationDays(30)
                .build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .uuid(UUID.randomUUID())
                .vendorId(7L)
                .build()));
        when(profileRepositoryPort.findByAccountId(20L)).thenReturn(List.of(ownerProfile, secondaryProfile));
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription input = invocation.getArgument(0);
            input.setUuid(UUID.randomUUID());
            return input;
        });

        service.confirm(orderUuid, profileUuid, "caller");

        assertThat(ownerProfile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(secondaryProfile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(fullAccount.getStatus()).isEqualTo(AccountStatus.FULL);
        verify(profileRepositoryPort).saveAll(List.of(ownerProfile, secondaryProfile));
        verify(accountRepositoryPort).save(fullAccount);
    }

    private void givenHappyPath(Integer durationDays) {
        givenValidatedOrder();
        when(profileRepositoryPort.findById(profileUuid)).thenReturn(Optional.of(Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .vendorId(7L)
                .accountId(20L)
                .status(ProfileStatus.AVAILABLE)
                .build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L)
                .uuid(UUID.randomUUID())
                .serviceId(30L)
                .build()));
        when(contextResolver.resolveSingleServiceForOrder(order)).thenReturn(Service.builder()
                .id(30L)
                .name("Netflix")
                .durationDays(durationDays)
                .build());
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .name("Netflix")
                .durationDays(durationDays)
                .build()));
        lenient().when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .uuid(UUID.randomUUID())
                .vendorId(7L)
                .build()));
    }

    private void givenValidatedOrder() {
        when(contextResolver.resolveCallerVendor("caller")).thenReturn(vendor);
        when(orderRepositoryPort.findByUuid(orderUuid)).thenReturn(Optional.of(order));
        when(subscriptionRepositoryPort.findByOrderId(11L)).thenReturn(Optional.empty());
    }
}
