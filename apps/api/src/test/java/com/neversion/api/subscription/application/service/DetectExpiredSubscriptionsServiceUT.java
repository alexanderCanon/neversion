package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService;

@ExtendWith(MockitoExtension.class)
@DisplayName("DetectExpiredSubscriptionsService — US-047 unit tests")
class DetectExpiredSubscriptionsServiceUT {

    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;

    private DetectExpiredSubscriptionsService detectExpiredSubscriptionsService;

    private static final Long VENDOR_ID = 10L;
    private static final Long PROFILE_ID = 30L;
    private static final Long ACCOUNT_ID = 40L;
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-29T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        detectExpiredSubscriptionsService = new DetectExpiredSubscriptionsService(
                subscriptionRepositoryPort,
                profileRepositoryPort,
                accountRepositoryPort,
                clientRepositoryPort,
                notificationLogPort,
                new InventoryStateDomainService(),
                FIXED_CLOCK);
    }

    private Subscription buildSubscription(Long id, Long vendorId, Long profileId) {
        return Subscription.builder()
                .id(id)
                .uuid(UUID.randomUUID())
                .vendorId(vendorId)
                .clientId(20L)
                .profileId(profileId)
                .paymentDueDate(LocalDate.of(2026, 4, 29))
                .status(SubStatus.ACTIVE)
                .build();
    }

    private Profile buildProfile(Long id) {
        return Profile.builder()
                .id(id)
                .uuid(UUID.randomUUID())
                .accountId(ACCOUNT_ID)
                .status(ProfileStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("detectAndSuspend")
    class DetectAndSuspend {

        @Test
        @DisplayName("should suspend BY_PROFILE subscription and expire profile")
        void detectAndSuspend_byProfile_shouldExpireProfile() {
            Subscription subscription = buildSubscription(1L, VENDOR_ID, PROFILE_ID);
            Profile profile = buildProfile(PROFILE_ID);
            Account account = Account.builder()
                    .id(ACCOUNT_ID).saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();

            when(subscriptionRepositoryPort.findOverdue(LocalDate.of(2026, 4, 29)))
                    .thenReturn(List.of(subscription));
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(profile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            int result = detectExpiredSubscriptionsService.detectAndSuspend();

            assertThat(result).isEqualTo(1);
            assertThat(subscription.getStatus()).isEqualTo(SubStatus.SUSPENDED);
            assertThat(profile.getStatus()).isEqualTo(ProfileStatus.EXPIRED);
            verify(profileRepositoryPort).saveAll(List.of(profile));
        }

        @Test
        @DisplayName("should suspend FULL_ACCOUNT subscription and expire all profiles")
        void detectAndSuspend_fullAccount_shouldExpireProfilesAndAccount() {
            Subscription subscription = buildSubscription(1L, VENDOR_ID, PROFILE_ID);
            Profile ownerProfile = buildProfile(PROFILE_ID);
            Profile secondaryProfile = Profile.builder()
                    .id(31L).uuid(UUID.randomUUID()).accountId(ACCOUNT_ID).status(ProfileStatus.ACTIVE).build();
            Account account = Account.builder()
                    .id(ACCOUNT_ID).saleMode(SaleMode.FULL_ACCOUNT).status(AccountStatus.FULL).build();

            when(subscriptionRepositoryPort.findOverdue(LocalDate.of(2026, 4, 29)))
                    .thenReturn(List.of(subscription));
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(ownerProfile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(profileRepositoryPort.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(ownerProfile, secondaryProfile));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            int result = detectExpiredSubscriptionsService.detectAndSuspend();

            assertThat(result).isEqualTo(1);
            assertThat(ownerProfile.getStatus()).isEqualTo(ProfileStatus.EXPIRED);
            assertThat(secondaryProfile.getStatus()).isEqualTo(ProfileStatus.EXPIRED);
            assertThat(account.getStatus()).isEqualTo(AccountStatus.EXPIRED);
            verify(profileRepositoryPort).saveAll(List.of(ownerProfile, secondaryProfile));
            verify(accountRepositoryPort).save(account);
        }

        @Test
        @DisplayName("should do nothing when there are no expired subscriptions")
        void detectAndSuspend_noExpired_shouldReturnZero() {
            when(subscriptionRepositoryPort.findOverdue(LocalDate.of(2026, 4, 29)))
                    .thenReturn(List.of());

            int result = detectExpiredSubscriptionsService.detectAndSuspend();

            assertThat(result).isZero();
            verify(notificationLogPort, never()).record(any(), any(), any());
            verify(notificationLogPort, never()).record(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("should aggregate vendor notification")
        void detectAndSuspend_multipleSameVendor_shouldRecordSingleSummary() {
            Subscription first = buildSubscription(1L, VENDOR_ID, PROFILE_ID);
            Subscription second = buildSubscription(2L, VENDOR_ID, 31L);
            Profile firstProfile = buildProfile(PROFILE_ID);
            Profile secondProfile = Profile.builder()
                    .id(31L).uuid(UUID.randomUUID()).accountId(41L).status(ProfileStatus.ACTIVE).build();
            Account firstAccount = Account.builder()
                    .id(ACCOUNT_ID).saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();
            Account secondAccount = Account.builder()
                    .id(41L).saleMode(SaleMode.BY_PROFILE).status(AccountStatus.PARTIAL).build();

            when(subscriptionRepositoryPort.findOverdue(LocalDate.of(2026, 4, 29)))
                    .thenReturn(List.of(first, second));
            when(profileRepositoryPort.findByInternalId(PROFILE_ID)).thenReturn(Optional.of(firstProfile));
            when(profileRepositoryPort.findByInternalId(31L)).thenReturn(Optional.of(secondProfile));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(firstAccount));
            when(accountRepositoryPort.findByInternalId(41L)).thenReturn(Optional.of(secondAccount));
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

            int result = detectExpiredSubscriptionsService.detectAndSuspend();

            assertThat(result).isEqualTo(2);
            verify(notificationLogPort).record(
                    eq("SUBSCRIPTIONS_EXPIRED_DAILY"),
                    eq("vendor:" + VENDOR_ID),
                    contains("\"expiredCount\":2"),
                    eq("vendor"), eq(VENDOR_ID), eq("expired_daily"));
        }
    }
}
