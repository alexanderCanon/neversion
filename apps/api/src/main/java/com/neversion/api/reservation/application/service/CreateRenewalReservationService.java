package com.neversion.api.reservation.application.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.reservation.application.port.in.CreateRenewalReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;

/**
 * EPIC-09 / US-061: Allows a client to request a renewal without directly
 * mutating the subscription. The actual renewal is applied only after vendor
 * approval of the payment receipt.
 */
@Service
public class CreateRenewalReservationService implements CreateRenewalReservationUseCase {

    private static final int EXPIRATION_MINUTES = 60;

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public CreateRenewalReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            ClientRepositoryPort clientRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    @Transactional
    public Reservation create(UUID subscriptionUuid, String paymentMethod, String callerExternalId) {
        Client client = resolveClient(callerExternalId);

        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionUuid));

        if (!client.getId().equals(subscription.getClientId())) {
            throw new AccessDeniedException("You do not have permission to renew this subscription.");
        }

        if (subscription.getStatus() != SubStatus.ACTIVE
                && subscription.getStatus() != SubStatus.SUSPENDED) {
            throw new BusinessRuleException("Only ACTIVE or SUSPENDED subscriptions can be renewed.");
        }

        if (reservationRepositoryPort.existsActiveRenewalBySubscriptionId(subscription.getId())) {
            throw new BusinessRuleException("There is already an active renewal reservation for this subscription.");
        }

        if (subscription.getServiceId() == null) {
            throw new BusinessRuleException("Subscription has no service snapshot for renewal.");
        }
        if (subscription.getPriceSold() == null) {
            throw new BusinessRuleException("Subscription has no price snapshot for renewal.");
        }

        BigDecimal discount = subscription.getDiscountApplied() != null
                ? subscription.getDiscountApplied()
                : BigDecimal.ZERO;

        OffsetDateTime now = OffsetDateTime.now();

        Reservation reservation = Reservation.builder()
                .clientId(client.getId())
                .clientUuid(client.getUuid())
                .vendorId(subscription.getVendorId())
                .status(ReservationStatus.PENDING)
                .discount(discount)
                .total(subscription.getPriceSold())
                .paymentMethod(paymentMethod)
                .expirationDate(now.plusMinutes(EXPIRATION_MINUTES).toInstant())
                .renewalSubscriptionId(subscription.getId())
                .renewalSubscriptionUuid(subscription.getUuid())
                .build();

        Reservation savedReservation = reservationRepositoryPort.save(reservation);

        ReservationDetail savedDetail = reservationRepositoryPort.saveDetail(new ReservationDetail(
                null,
                null,
                savedReservation.getId(),
                subscription.getServiceId(),
                1,
                subscription.getPriceSold(),
                null));

        savedReservation.setClientUuid(client.getUuid());
        savedReservation.setRenewalSubscriptionId(subscription.getId());
        savedReservation.setRenewalSubscriptionUuid(subscription.getUuid());
        savedReservation.setDetails(List.of(savedDetail));
        return savedReservation;
    }

    private Client resolveClient(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));

        return clientRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client record not found for userId: " + user.getId()));
    }
}

