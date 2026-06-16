package com.neversion.api.notification.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.notification.application.port.in.SendAccountRenewalRemindersUseCase;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Records operational reminders for master account renewal dates.
 */
@Service
public class SendAccountRenewalRemindersService implements SendAccountRenewalRemindersUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendAccountRenewalRemindersService.class);
    private static final String ENTITY_TYPE = "account";

    private static final List<ReminderWindow> REMINDER_WINDOWS = List.of(
            new ReminderWindow(7, "account_renewal_7d", "ACCOUNT_RENEWAL_REMINDER_7D"),
            new ReminderWindow(3, "account_renewal_3d", "ACCOUNT_RENEWAL_REMINDER_3D"),
            new ReminderWindow(1, "account_renewal_1d", "ACCOUNT_RENEWAL_REMINDER_1D"),
            new ReminderWindow(0, "account_renewal_due", "ACCOUNT_RENEWAL_REMINDER_DUE")
    );

    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AuthServicePort authServicePort;
    private final NotificationLogPort notificationLogPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SendAccountRenewalRemindersService(
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            AuthServicePort authServicePort,
            NotificationLogPort notificationLogPort,
            ObjectMapper objectMapper,
            Clock clock) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.authServicePort = authServicePort;
        this.notificationLogPort = notificationLogPort;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int sendReminders() {
        LocalDate today = LocalDate.now(clock);
        int totalRecorded = 0;

        for (ReminderWindow window : REMINDER_WINDOWS) {
            LocalDate targetDate = today.plusDays(window.daysAhead());
            List<Account> accounts = accountRepositoryPort.findByRenewalDate(targetDate);

            for (Account account : accounts) {
                if (notificationLogPort.existsByEntityAndStage(ENTITY_TYPE, account.getId(), window.stage())) {
                    continue;
                }

                try {
                    Optional<VendorRecipient> recipient = resolveVendorRecipient(account);
                    if (recipient.isEmpty()) {
                        log.warn("Cannot record account renewal reminder for account {}: vendor recipient not found",
                                account.getUuid());
                        continue;
                    }

                    String payload = buildPayload(account, recipient.get(), window.daysAhead());
                    notificationLogPort.record(
                            window.notificationType(),
                            recipient.get().email(),
                            payload,
                            ENTITY_TYPE,
                            account.getId(),
                            window.stage());
                    totalRecorded++;
                } catch (Exception ex) {
                    log.error("Error recording account renewal reminder for account {}: {}",
                            account.getUuid(), ex.getMessage());
                }
            }
        }

        if (totalRecorded > 0) {
            log.info("Recorded {} master account renewal reminders", totalRecorded);
        }
        return totalRecorded;
    }

    private Optional<VendorRecipient> resolveVendorRecipient(Account account) {
        Optional<Vendor> vendor = vendorRepositoryPort.findByInternalId(account.getVendorId());
        if (vendor.isEmpty()) {
            return Optional.empty();
        }

        return userRepositoryPort.findById(vendor.get().getUserId())
                .flatMap(user -> authServicePort.findEmailByExternalId(user.getExternalId())
                        .map(email -> new VendorRecipient(
                                email,
                                vendor.get().getUuid().toString(),
                                user.getExternalId(),
                                vendor.get().getStoreName())));
    }

    private String buildPayload(Account account, VendorRecipient recipient, int daysRemaining)
            throws JsonProcessingException {
        String serviceName = serviceRepositoryPort.findByInternalId(account.getServiceId())
                .map(com.neversion.api.service.domain.model.Service::getName)
                .orElse("Servicio");

        return objectMapper.writeValueAsString(Map.of(
                "accountId", account.getUuid().toString(),
                "vendorId", recipient.vendorId(),
                "vendorExternalId", recipient.vendorExternalId(),
                "storeName", recipient.storeName(),
                "serviceName", serviceName,
                "accountEmail", account.getEmail(),
                "renewalDate", account.getRenewalDate().toString(),
                "daysRemaining", daysRemaining));
    }

    private record ReminderWindow(int daysAhead, String stage, String notificationType) {
    }

    private record VendorRecipient(String email, String vendorId, String vendorExternalId, String storeName) {
    }
}
