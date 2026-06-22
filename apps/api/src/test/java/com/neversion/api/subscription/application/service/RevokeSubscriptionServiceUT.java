package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevokeSubscriptionService — US-046 unit tests")
class RevokeSubscriptionServiceUT {

    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    private RevokeSubscriptionService revokeSubscriptionService;

    private static final UUID SUBSCRIPTION_UUID = UUID.randomUUID();
    private static final String EXTERNAL_ID = "auth|revoke";
    private static final Long USER_ID = 5L;
    private static final Long VENDOR_ID = 10L;
    private static final Long CLIENT_ID = 20L;
    private static final Long PROFILE_ID = 30L;
    private static final Long ACCOUNT_ID = 40L;

    @BeforeEach
    void setUp() {
        revokeSubscriptionService = new RevokeSubscriptionService(
                subscriptionRepositoryPort,
                profileRepositoryPort,
                accountRepositoryPort,
                clientRepositoryPort,
                serviceRepositoryPort,
                notificationLogPort,
                new InventoryStateDomainService(),
                new VendorSecurityService(userRepositoryPort, vendorRepositoryPort),
                historyRepositoryPort);
    }

    private void mockOwnershipResolution(Long vendorId) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).role(UserRole.VENDOR).build();
        Vendor vendor = Vendor.builder().id(vendorId).userId(USER_ID).storeName("Vendor").build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Subscription buildSubscription(SubStatus status, Long vendorId) {
        return Subscription.builder()
                .id(1L)
                .uuid(SUBSCRIPTION_UUID)
                .vendorId(vendorId)
                .clientId(CLIENT_ID)
                .profileId(PROFILE_ID)
                .paymentDueDate(LocalDate.now().plusDays(10))
                .monthsPaid(1L)
                .status(status)
                .build();
    }

    private Client buildClient() {
        return Client.builder()
                .id(CLIENT_ID)
                .uuid(UUID.randomUUID())
                .email("client@test.com")
                .name("Client")
                .build();
    }

    private Profile buildProfile() {
        return Profile.builder()
                .id(PROFILE_ID)
                .uuid(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .status(ProfileStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("revoke")
    class Revoke {

        @Test
        @DisplayName("should cancel BY_PROFILE subscription and release profile")
        void revoke_byProfile_shouldReleaseProfile() {
            Subscription subscription = buildSubscription(SubStatus.ACTIVE, VENDOR_ID);
            Profile profile = buildProfile();
            Account account = Account.builder()
                    .id(ACCOUNT_ID).saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();
            Client client = buildClient();

            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID)).thenReturn(Optional.of(subscription));
            mockOwnershipResolution(VENDOR_ID);
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(profile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(client));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = revokeSubscriptionService.revoke(SUBSCRIPTION_UUID, EXTERNAL_ID);

            assertThat(result.getStatus()).isEqualTo(SubStatus.CANCELLED);
            assertThat(profile.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
            verify(profileRepositoryPort).saveAll(List.of(profile));
            verify(notificationLogPort).record(eq("ACCESS_REVOKED"), eq("client@test.com"), contains(SUBSCRIPTION_UUID.toString()),
                    eq("subscription"), eq(1L), eq("revoked"));
        }

        @Test
        @DisplayName("should cancel FULL_ACCOUNT subscription and release account profiles")
        void revoke_fullAccount_shouldReleaseProfilesAndAccount() {
            Subscription subscription = buildSubscription(SubStatus.SUSPENDED, VENDOR_ID);
            Profile ownerProfile = buildProfile();
            Profile secondaryProfile = Profile.builder()
                    .id(31L).uuid(UUID.randomUUID()).accountId(ACCOUNT_ID).status(ProfileStatus.ACTIVE).build();
            Account account = Account.builder()
                    .id(ACCOUNT_ID).saleMode(SaleMode.FULL_ACCOUNT).status(AccountStatus.FULL).build();
            Client client = buildClient();

            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID)).thenReturn(Optional.of(subscription));
            mockOwnershipResolution(VENDOR_ID);
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(ownerProfile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(client));
            when(profileRepositoryPort.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(ownerProfile, secondaryProfile));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            Subscription result = revokeSubscriptionService.revoke(SUBSCRIPTION_UUID, EXTERNAL_ID);

            assertThat(result.getStatus()).isEqualTo(SubStatus.CANCELLED);
            assertThat(ownerProfile.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
            assertThat(secondaryProfile.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
            assertThat(account.getStatus()).isEqualTo(AccountStatus.AVAILABLE);
            verify(profileRepositoryPort).saveAll(List.of(ownerProfile, secondaryProfile));
            verify(accountRepositoryPort).save(account);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when caller does not own subscription")
        void revoke_notOwned_shouldThrow403() {
            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                    .thenReturn(Optional.of(buildSubscription(SubStatus.ACTIVE, 99L)));
            mockOwnershipResolution(VENDOR_ID);

            assertThatThrownBy(() -> revokeSubscriptionService.revoke(SUBSCRIPTION_UUID, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should throw BusinessRuleException when subscription is already cancelled")
        void revoke_alreadyCancelled_shouldThrow409() {
            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID))
                    .thenReturn(Optional.of(buildSubscription(SubStatus.CANCELLED, VENDOR_ID)));
            mockOwnershipResolution(VENDOR_ID);

            assertThatThrownBy(() -> revokeSubscriptionService.revoke(SUBSCRIPTION_UUID, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("should record access revoked notification")
        void revoke_validRequest_shouldRecordNotification() {
            Subscription subscription = buildSubscription(SubStatus.ACTIVE, VENDOR_ID);
            Profile profile = buildProfile();
            Account account = Account.builder()
                    .id(ACCOUNT_ID).saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();
            Client client = buildClient();

            when(subscriptionRepositoryPort.findById(SUBSCRIPTION_UUID)).thenReturn(Optional.of(subscription));
            mockOwnershipResolution(VENDOR_ID);
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(profile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(clientRepositoryPort.findByInternalId(CLIENT_ID)).thenReturn(Optional.of(client));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            revokeSubscriptionService.revoke(SUBSCRIPTION_UUID, EXTERNAL_ID);

            verify(notificationLogPort).record(
                    eq("ACCESS_REVOKED"),
                    eq("client@test.com"),
                    contains(SUBSCRIPTION_UUID.toString()),
                    eq("subscription"), eq(1L), eq("revoked"));
        }
    }
}
