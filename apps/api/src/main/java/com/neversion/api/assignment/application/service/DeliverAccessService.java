package com.neversion.api.assignment.application.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.enums.ProfileDeliveryType;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountPreference;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;

@Service
public class DeliverAccessService implements DeliverAccessUseCase {

    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final NotificationPayloadWriter payloadWriter;

    public DeliverAccessService(
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            NotificationLogPort notificationLogPort,
            NotificationPayloadWriter payloadWriter) {
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.payloadWriter = payloadWriter;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliver(Subscription subscription) {
        var profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for subscription."));

        var account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for profile."));

        var service = serviceRepositoryPort.findByInternalId(account.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for account."));

        var client = clientRepositoryPort.findByInternalId(subscription.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found for subscription."));

        /*
         * Detect if this account delivers access via personal account invitation
         * (e.g. Spotify Family). If so, the master credentials are NEVER sent.
         */
        boolean isPersonalAccount = account.getProfileDeliveryType() == ProfileDeliveryType.PERSONAL_ACCOUNT;

        /*
         * Check checkout preference: if CUENTA_PROPIA, followUpViaWhatsapp is true
         * and credentials are not sent at all.
         */
        boolean isCuentaPropia = isPersonalAccount
                && subscription.getAccountPreference() == AccountPreference.CUENTA_PROPIA;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getUuid());
        payload.put("serviceName", service.getName());
        payload.put("clientName", client.getName());
        payload.put("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : null);

        if (isCuentaPropia) {
            /*
             * Client selected "Cuenta propia": they already have an account.
             * No credentials sent — just a follow-up via WhatsApp notice.
             */
            payload.put("followUpViaWhatsapp", true);
        } else if (isPersonalAccount) {
            /*
             * Client selected "Cuenta nueva": vendor creates a new account for the client.
             * Master account credentials are withheld. profileName = personal email, pin = password.
             */
            payload.put("followUpViaWhatsapp", false);
            payload.put("profileName", profile.getName());
            if (profile.getPin() != null) {
                payload.put("pin", profile.getPin());
            }
        } else {
            /*
             * Standard service (Netflix, HBO Max, Disney+, etc.):
             * deliver master credentials + profile details.
             */
            payload.put("accountEmail", account.getEmail());
            payload.put("accountPassword", account.getPassword());
            payload.put("profileName", profile.getName());
            if (profile.getPin() != null) {
                payload.put("pin", profile.getPin());
            }
        }

        notificationLogPort.record("ACCESS_DELIVERED", client.getEmail(), payloadWriter.write(payload),
                "subscription", subscription.getId(), "access_delivered");
    }
}
