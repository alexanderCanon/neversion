package com.neversion.api.assignment.application.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;

@Service
public class DeliverAccessService implements DeliverAccessUseCase {

    private static final String SPOTIFY_SERVICE_NAME = "Spotify";

    /**
     * Notes value written by the storefront when the client selects
     * "Cuenta propia" at Spotify checkout.
     */
    private static final String CUENTA_PROPIA = "CUENTA_PROPIA";

    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final NotificationPayloadWriter payloadWriter;

    public DeliverAccessService(
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            NotificationLogPort notificationLogPort,
            NotificationPayloadWriter payloadWriter) {
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
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
         * Spotify Family (BY_PROFILE): the master account credentials (email + password)
         * must NEVER be sent to the client — exposing them would compromise the vendor's
         * anchor account for all family slots.
         *
         * For every other service the master credentials are included as usual.
         */
        boolean isSpotifyByProfile = SPOTIFY_SERVICE_NAME.equalsIgnoreCase(service.getName())
                && account.getSaleMode() == SaleMode.BY_PROFILE;

        /*
         * For Spotify checkouts, the storefront writes the client's preference into
         * the order notes: "CUENTA_PROPIA" means the client already has their own
         * Spotify account and only needs a WhatsApp follow-up.
         * Any other value (including "CUENTA_NUEVA" or null) means the vendor will
         * create a new Spotify account — credentials are delivered by email.
         *
         * For non-Spotify services this flag is always false.
         */
        boolean isSpotifyCuentaPropia = false;
        if (isSpotifyByProfile && subscription.getOrderId() != null) {
            isSpotifyCuentaPropia = orderRepositoryPort.findByInternalId(subscription.getOrderId())
                    .map(Order::getNotes)
                    .map(notes -> notes.contains(CUENTA_PROPIA))
                    .orElse(false);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getUuid());
        payload.put("serviceName", service.getName());
        payload.put("clientName", client.getName());
        payload.put("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : null);

        if (isSpotifyCuentaPropia) {
            /*
             * Client selected "Cuenta propia": they already have a Spotify account.
             * No credentials are sent — just a WhatsApp follow-up notice so the
             * vendor can add them to the Family plan manually.
             */
            payload.put("followUpViaWhatsapp", true);
        } else if (isSpotifyByProfile) {
            /*
             * Client selected "Cuenta nueva": the vendor will create a fresh Spotify
             * account. profileName = personal email, pin = password for that account.
             * Master account credentials are still withheld.
             */
            payload.put("followUpViaWhatsapp", false);
            payload.put("profileName", profile.getName());
            if (profile.getPin() != null) {
                payload.put("pin", profile.getPin());
            }
        } else {
            /*
             * Standard service (Netflix, HBO Max, Disney+, etc.):
             * deliver master account credentials + profile slot details.
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
