package com.neversion.api.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
class SendRenewalRemindersServiceUT {

    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private AuthServicePort authServicePort;
    @Mock private NotificationLogPort notificationLogPort;

    private SendRenewalRemindersService service;

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 1);
    private final Clock fixedClock = Clock.fixed(
            ZonedDateTime.of(TODAY.atStartOfDay(), ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        service = new SendRenewalRemindersService(
                subscriptionRepositoryPort, clientRepositoryPort,
                profileRepositoryPort, accountRepositoryPort,
                serviceRepositoryPort, vendorRepositoryPort,
                userRepositoryPort, authServicePort,
                notificationLogPort, fixedClock);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Subscription buildSubscription(Long id, LocalDate dueDate) {
        return Subscription.builder()
                .id(id)
                .uuid(UUID.randomUUID())
                .clientId(100L)
                .profileId(200L)
                .paymentDueDate(dueDate)
                .status(SubStatus.ACTIVE)
                .build();
    }

    private Client buildClient(String email) {
        return Client.builder()
                .id(100L)
                .uuid(UUID.randomUUID())
                .name("Juan")
                .email(email)
                .build();
    }

    /** Stubs the vendor resolution chain for the given profileId → accountId → vendorId path. */
    private void stubVendorChain(String vendorEmail) {
        Profile profile = Profile.builder().id(200L).accountId(300L).build();
        Account account = Account.builder().id(300L).vendorId(400L).build();
        Vendor vendor = Vendor.builder().id(400L).uuid(UUID.randomUUID()).userId(500L).storeName("MiTienda").build();
        User user = User.builder().id(500L).externalId("ext-uuid-vendor").build();

        when(profileRepositoryPort.findByInternalId(200L)).thenReturn(Optional.of(profile));
        when(accountRepositoryPort.findByInternalId(300L)).thenReturn(Optional.of(account));
        when(vendorRepositoryPort.findByInternalId(400L)).thenReturn(Optional.of(vendor));
        when(userRepositoryPort.findById(500L)).thenReturn(Optional.of(user));
        when(authServicePort.findEmailByExternalId("ext-uuid-vendor")).thenReturn(Optional.ofNullable(vendorEmail));
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sendReminders_clientAndVendorHaveEmail_recordsBoth")
    void sendReminders_clientAndVendorHaveEmail_recordsBoth() {
        LocalDate dueIn7Days = TODAY.plusDays(7);
        Subscription sub = buildSubscription(1L, dueIn7Days);

        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(dueIn7Days)).thenReturn(List.of(sub));
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(3))).thenReturn(List.of());
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(1))).thenReturn(List.of());
        when(notificationLogPort.existsByEntityAndStage(anyString(), anyLong(), anyString())).thenReturn(false);
        when(clientRepositoryPort.findByInternalId(100L)).thenReturn(Optional.of(buildClient("juan@test.com")));
        stubVendorChain("vendor@test.com");

        int result = service.sendReminders();

        assertEquals(2, result);
        verify(notificationLogPort).record(
                eq("RENEWAL_REMINDER_7D"), eq("juan@test.com"), anyString(),
                eq("subscription"), eq(1L), eq("reminder_7d_client"));
        verify(notificationLogPort).record(
                eq("RENEWAL_REMINDER_7D"), eq("vendor@test.com"), anyString(),
                eq("subscription"), eq(1L), eq("reminder_7d_vendor"));
    }

    @Test
    @DisplayName("sendReminders_clientHasNoEmail_onlyVendorNotified")
    void sendReminders_clientHasNoEmail_onlyVendorNotified() {
        LocalDate dueIn7Days = TODAY.plusDays(7);
        Subscription sub = buildSubscription(1L, dueIn7Days);

        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(dueIn7Days)).thenReturn(List.of(sub));
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(3))).thenReturn(List.of());
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(1))).thenReturn(List.of());
        when(notificationLogPort.existsByEntityAndStage(anyString(), anyLong(), anyString())).thenReturn(false);
        // Client has no email
        when(clientRepositoryPort.findByInternalId(100L)).thenReturn(Optional.of(buildClient(null)));
        stubVendorChain("vendor@test.com");

        int result = service.sendReminders();

        assertEquals(1, result);
        verify(notificationLogPort, never()).record(
                anyString(), anyString(), anyString(),
                eq("subscription"), eq(1L), eq("reminder_7d_client"));
        verify(notificationLogPort).record(
                eq("RENEWAL_REMINDER_7D"), eq("vendor@test.com"), anyString(),
                eq("subscription"), eq(1L), eq("reminder_7d_vendor"));
    }

    @Test
    @DisplayName("sendReminders_vendorChainUnresolvable_onlyClientNotified")
    void sendReminders_vendorChainUnresolvable_onlyClientNotified() {
        LocalDate dueIn7Days = TODAY.plusDays(7);
        Subscription sub = buildSubscription(1L, dueIn7Days);

        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(dueIn7Days)).thenReturn(List.of(sub));
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(3))).thenReturn(List.of());
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(1))).thenReturn(List.of());
        when(notificationLogPort.existsByEntityAndStage(anyString(), anyLong(), anyString())).thenReturn(false);
        when(clientRepositoryPort.findByInternalId(100L)).thenReturn(Optional.of(buildClient("juan@test.com")));
        // Vendor chain broken: profile not found
        when(profileRepositoryPort.findByInternalId(200L)).thenReturn(Optional.empty());

        int result = service.sendReminders();

        assertEquals(1, result);
        verify(notificationLogPort).record(
                eq("RENEWAL_REMINDER_7D"), eq("juan@test.com"), anyString(),
                eq("subscription"), eq(1L), eq("reminder_7d_client"));
        verify(notificationLogPort, never()).record(
                anyString(), anyString(), anyString(),
                eq("subscription"), eq(1L), eq("reminder_7d_vendor"));
    }

    @Test
    @DisplayName("sendReminders_clientAndVendorDeduped_skipsAll")
    void sendReminders_clientAndVendorDeduped_skipsAll() {
        LocalDate dueIn3Days = TODAY.plusDays(3);
        Subscription sub = buildSubscription(2L, dueIn3Days);

        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(7))).thenReturn(List.of());
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(dueIn3Days)).thenReturn(List.of(sub));
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(1))).thenReturn(List.of());
        // Both already sent
        when(notificationLogPort.existsByEntityAndStage("subscription", 2L, "reminder_3d_client")).thenReturn(true);
        when(notificationLogPort.existsByEntityAndStage("subscription", 2L, "reminder_3d_vendor")).thenReturn(true);
        when(clientRepositoryPort.findByInternalId(100L)).thenReturn(Optional.of(buildClient("juan@test.com")));
        stubVendorChain("vendor@test.com");

        int result = service.sendReminders();

        assertEquals(0, result);
        verify(notificationLogPort, never()).record(
                anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("sendReminders_noSubscriptionsDue_returnsZero")
    void sendReminders_noSubscriptionsDue_returnsZero() {
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(any())).thenReturn(List.of());

        int result = service.sendReminders();

        assertEquals(0, result);
        verify(notificationLogPort, never()).record(
                anyString(), anyString(), anyString(),
                anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("sendReminders_1dHappyPath_recordsBothRecipients")
    void sendReminders_1dHappyPath_recordsBothRecipients() {
        LocalDate dueIn1Day = TODAY.plusDays(1);
        Subscription sub = buildSubscription(3L, dueIn1Day);

        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(7))).thenReturn(List.of());
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(3))).thenReturn(List.of());
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(dueIn1Day)).thenReturn(List.of(sub));
        when(notificationLogPort.existsByEntityAndStage(anyString(), anyLong(), anyString())).thenReturn(false);
        when(clientRepositoryPort.findByInternalId(100L)).thenReturn(Optional.of(buildClient("juan@test.com")));
        stubVendorChain("vendor@test.com");

        int result = service.sendReminders();

        assertEquals(2, result);
        verify(notificationLogPort).record(
                eq("RENEWAL_REMINDER_1D"), eq("juan@test.com"), anyString(),
                eq("subscription"), eq(3L), eq("reminder_1d_client"));
        verify(notificationLogPort).record(
                eq("RENEWAL_REMINDER_1D"), eq("vendor@test.com"), anyString(),
                eq("subscription"), eq(3L), eq("reminder_1d_vendor"));
    }

    @Test
    @DisplayName("sendReminders_multipleIntervals_recordsAll")
    void sendReminders_multipleIntervals_recordsAll() {
        Subscription sub7 = buildSubscription(10L, TODAY.plusDays(7));
        Subscription sub3 = buildSubscription(20L, TODAY.plusDays(3));

        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(7))).thenReturn(List.of(sub7));
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(3))).thenReturn(List.of(sub3));
        when(subscriptionRepositoryPort.findActiveByPaymentDueDate(TODAY.plusDays(1))).thenReturn(List.of());
        when(notificationLogPort.existsByEntityAndStage(anyString(), anyLong(), anyString())).thenReturn(false);
        when(clientRepositoryPort.findByInternalId(100L)).thenReturn(Optional.of(buildClient("juan@test.com")));
        stubVendorChain("vendor@test.com");

        int result = service.sendReminders();

        // 2 subs × 2 recipients each = 4 records
        assertEquals(4, result);
    }
}
