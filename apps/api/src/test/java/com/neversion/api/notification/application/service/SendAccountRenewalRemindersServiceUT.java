package com.neversion.api.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
class SendAccountRenewalRemindersServiceUT {

    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private AuthServicePort authServicePort;
    @Mock private NotificationLogPort notificationLogPort;

    private SendAccountRenewalRemindersService service;

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 1);
    private final Clock fixedClock = Clock.fixed(
            ZonedDateTime.of(TODAY.atStartOfDay(), ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        service = new SendAccountRenewalRemindersService(
                accountRepositoryPort,
                serviceRepositoryPort,
                vendorRepositoryPort,
                userRepositoryPort,
                authServicePort,
                notificationLogPort,
                new ObjectMapper(),
                fixedClock);
    }

    @Test
    @DisplayName("sendReminders_7dHappyPath_recordsReminder")
    void sendReminders_7dHappyPath_recordsReminder() {
        Account account = buildAccount(10L, TODAY.plusDays(7));
        when(accountRepositoryPort.findByRenewalDate(any())).thenReturn(List.of());
        when(accountRepositoryPort.findByRenewalDate(TODAY.plusDays(7))).thenReturn(List.of(account));
        when(notificationLogPort.existsByEntityAndStage("account", 10L, "account_renewal_7d"))
                .thenReturn(false);
        mockVendorRecipient();
        mockServiceName();

        int result = service.sendReminders();

        assertEquals(1, result);
        verify(notificationLogPort).record(
                eq("ACCOUNT_RENEWAL_REMINDER_7D"),
                eq("vendor@test.com"),
                contains("\"serviceName\":\"Netflix\""),
                eq("account"),
                eq(10L),
                eq("account_renewal_7d"));
    }

    @Test
    @DisplayName("sendReminders_alreadySent_skipsDedup")
    void sendReminders_alreadySent_skipsDedup() {
        Account account = buildAccount(20L, TODAY.plusDays(3));
        when(accountRepositoryPort.findByRenewalDate(any())).thenReturn(List.of());
        when(accountRepositoryPort.findByRenewalDate(TODAY.plusDays(3))).thenReturn(List.of(account));
        when(notificationLogPort.existsByEntityAndStage("account", 20L, "account_renewal_3d"))
                .thenReturn(true);

        int result = service.sendReminders();

        assertEquals(0, result);
        verify(notificationLogPort, never()).record(
                anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("sendReminders_vendorEmailNotFound_skipsReminder")
    void sendReminders_vendorEmailNotFound_skipsReminder() {
        Account account = buildAccount(30L, TODAY.plusDays(1));
        when(accountRepositoryPort.findByRenewalDate(any())).thenReturn(List.of());
        when(accountRepositoryPort.findByRenewalDate(TODAY.plusDays(1))).thenReturn(List.of(account));
        when(notificationLogPort.existsByEntityAndStage("account", 30L, "account_renewal_1d"))
                .thenReturn(false);
        when(vendorRepositoryPort.findByInternalId(7L)).thenReturn(Optional.of(buildVendor()));
        when(userRepositoryPort.findById(100L)).thenReturn(Optional.of(buildUser()));
        when(authServicePort.findEmailByExternalId("supabase-vendor-id")).thenReturn(Optional.empty());

        int result = service.sendReminders();

        assertEquals(0, result);
        verify(notificationLogPort, never()).record(
                anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("sendReminders_dueToday_recordsDueReminder")
    void sendReminders_dueToday_recordsDueReminder() {
        Account account = buildAccount(40L, TODAY);
        when(accountRepositoryPort.findByRenewalDate(any())).thenReturn(List.of());
        when(accountRepositoryPort.findByRenewalDate(TODAY)).thenReturn(List.of(account));
        when(notificationLogPort.existsByEntityAndStage("account", 40L, "account_renewal_due"))
                .thenReturn(false);
        mockVendorRecipient();
        mockServiceName();

        int result = service.sendReminders();

        assertEquals(1, result);
        verify(notificationLogPort).record(
                eq("ACCOUNT_RENEWAL_REMINDER_DUE"),
                eq("vendor@test.com"),
                contains("\"daysRemaining\":0"),
                eq("account"),
                eq(40L),
                eq("account_renewal_due"));
    }

    private Account buildAccount(Long id, LocalDate renewalDate) {
        return Account.builder()
                .id(id)
                .uuid(UUID.randomUUID())
                .serviceId(5L)
                .vendorId(7L)
                .email("matrix@test.com")
                .renewalDate(renewalDate)
                .build();
    }

    private void mockVendorRecipient() {
        when(vendorRepositoryPort.findByInternalId(7L)).thenReturn(Optional.of(buildVendor()));
        when(userRepositoryPort.findById(100L)).thenReturn(Optional.of(buildUser()));
        when(authServicePort.findEmailByExternalId("supabase-vendor-id")).thenReturn(Optional.of("vendor@test.com"));
    }

    private void mockServiceName() {
        when(serviceRepositoryPort.findByInternalId(5L))
                .thenReturn(Optional.of(com.neversion.api.service.domain.model.Service.builder()
                        .id(5L)
                        .uuid(UUID.randomUUID())
                        .name("Netflix")
                        .build()));
    }

    private Vendor buildVendor() {
        return Vendor.builder()
                .id(7L)
                .uuid(UUID.randomUUID())
                .userId(100L)
                .storeName("Neversion")
                .build();
    }

    private User buildUser() {
        return User.builder()
                .id(100L)
                .externalId("supabase-vendor-id")
                .role(UserRole.VENDOR)
                .build();
    }

}
