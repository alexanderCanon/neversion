package com.neversion.api.assignment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManualAssignmentService Unit Tests")
class ManualAssignmentServiceUT {

    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private DeliverAccessUseCase deliverAccessUseCase;
    @Mock private AssignmentContextResolver contextResolver;
    @Mock private ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    private ManualAssignmentService service;
    private UUID clientUuid;
    private UUID serviceUuid;
    private UUID profileUuid;

    @BeforeEach
    void setUp() {
        service = new ManualAssignmentService(
                clientRepositoryPort,
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                subscriptionRepositoryPort,
                deliverAccessUseCase,
                contextResolver,
                historyRepositoryPort);
        clientUuid = UUID.randomUUID();
        serviceUuid = UUID.randomUUID();
        profileUuid = UUID.randomUUID();
    }

    @Test
    @DisplayName("assign_shouldCreateSubscription_withVendorDates_andNotifyClient")
    void assign_shouldCreateSubscription_withVendorDates_andNotifyClient() {
        givenHappyPath();
        UUID subscriptionUuid = UUID.randomUUID();
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription input = invocation.getArgument(0);
            input.setUuid(subscriptionUuid);
            return input;
        });

        AssignmentResult result = service.assign(
                clientUuid,
                serviceUuid,
                profileUuid,
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 29),
                "caller");

        assertThat(result.subscriptionUuid()).isEqualTo(subscriptionUuid);
        assertThat(result.orderUuid()).isNull();
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 4, 29));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 5, 29));

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepositoryPort).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue().getOrderId()).isNull();
        assertThat(subscriptionCaptor.getValue().getPaymentDueDate()).isEqualTo(LocalDate.of(2026, 5, 29));
        verify(deliverAccessUseCase).deliver(any(Subscription.class));
    }

    @Test
    @DisplayName("assign_shouldThrow400_whenProfileNotAvailable")
    void assign_shouldThrow400_whenProfileNotAvailable() {
        givenBaseOwnership();
        when(profileRepositoryPort.findById(profileUuid)).thenReturn(Optional.of(Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .vendorId(7L)
                .status(ProfileStatus.BLOCKED)
                .build()));

        assertThatThrownBy(() -> service.assign(clientUuid, serviceUuid, profileUuid,
                LocalDate.now(), LocalDate.now().plusDays(30), "caller"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("AVAILABLE");
    }

    @Test
    @DisplayName("assign_shouldThrow409_whenProfileAlreadyHasActiveSubscription")
    void assign_shouldThrow409_whenProfileAlreadyHasActiveSubscription() {
        givenBaseOwnership();
        when(profileRepositoryPort.findById(profileUuid)).thenReturn(Optional.of(Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .vendorId(7L)
                .status(ProfileStatus.AVAILABLE)
                .build()));
        when(subscriptionRepositoryPort.existsActiveByProfileId(50L)).thenReturn(true);

        assertThatThrownBy(() -> service.assign(clientUuid, serviceUuid, profileUuid,
                LocalDate.now(), LocalDate.now().plusDays(30), "caller"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("active subscription");
    }

    @Test
    @DisplayName("assign_fullAccount_shouldActivateAllProfiles_andMarkAccountFull")
    void assign_fullAccount_shouldActivateAllProfiles_andMarkAccountFull() {
        givenBaseOwnership();
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
        when(subscriptionRepositoryPort.existsActiveByProfileId(50L)).thenReturn(false);
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(fullAccount));
        when(profileRepositoryPort.findByAccountId(20L)).thenReturn(List.of(ownerProfile, secondaryProfile));
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription input = invocation.getArgument(0);
            input.setUuid(UUID.randomUUID());
            return input;
        });

        service.assign(clientUuid, serviceUuid, profileUuid,
                LocalDate.of(2026, 4, 29),
                LocalDate.of(2026, 5, 29),
                "caller");

        assertThat(ownerProfile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(secondaryProfile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(fullAccount.getStatus()).isEqualTo(AccountStatus.FULL);
        verify(profileRepositoryPort).saveAll(List.of(ownerProfile, secondaryProfile));
        verify(accountRepositoryPort).save(fullAccount);
    }

    private void givenHappyPath() {
        givenBaseOwnership();
        when(profileRepositoryPort.findById(profileUuid)).thenReturn(Optional.of(Profile.builder()
                .id(50L)
                .uuid(profileUuid)
                .vendorId(7L)
                .accountId(20L)
                .status(ProfileStatus.AVAILABLE)
                .build()));
        when(subscriptionRepositoryPort.existsActiveByProfileId(50L)).thenReturn(false);
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L)
                .uuid(UUID.randomUUID())
                .serviceId(30L)
                .build()));
    }

    private void givenBaseOwnership() {
        when(contextResolver.resolveCallerVendor("caller")).thenReturn(Vendor.builder().id(7L).build());
        when(clientRepositoryPort.findById(clientUuid)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .uuid(clientUuid)
                .vendorId(7L)
                .build()));
        when(serviceRepositoryPort.findById(serviceUuid)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .uuid(serviceUuid)
                .vendorId(7L)
                .name("Netflix")
                .build()));
    }
}
