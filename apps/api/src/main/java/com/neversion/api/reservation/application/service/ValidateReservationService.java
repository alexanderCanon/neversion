package com.neversion.api.reservation.application.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.application.port.in.CreateOrderUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.reservation.application.port.in.ValidateReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.application.port.in.RenewSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * UC3: Validate and Formalize Sale — US-035.
 * <p>
 * Admin/Vendor validates the payment receipt, transitions the reservation from
 * UPLOADED → VALIDATED, and triggers creation of the Order.
 * Includes ownership check to ensure vendors only validate their own reservations.
 * </p>
 */
@Service
public class ValidateReservationService implements ValidateReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final CreateOrderUseCase createOrderUseCase;
    private final RenewSubscriptionUseCase renewSubscriptionUseCase;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderStatusHistoryPort orderStatusHistoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final NotificationLogPort notificationLogPort;

    public ValidateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            CreateOrderUseCase createOrderUseCase,
            RenewSubscriptionUseCase renewSubscriptionUseCase,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            OrderStatusHistoryPort orderStatusHistoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            NotificationLogPort notificationLogPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.createOrderUseCase = createOrderUseCase;
        this.renewSubscriptionUseCase = renewSubscriptionUseCase;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderStatusHistoryPort = orderStatusHistoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.notificationLogPort = notificationLogPort;
    }

    @Override
    @Transactional
    public Reservation validate(UUID reservationId, String notes, String callerExternalId) {

        // 1. Resolve caller vendor for ownership check
        User caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));
        
        Vendor vendor = vendorRepositoryPort.findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for caller"));

        // 2. Load and validate reservation
        Reservation reservation = reservationRepositoryPort.findByUuid(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + reservationId));

        // Ownership check (ADR-02)
        if (!reservation.getVendorId().equals(vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to validate this reservation.");
        }

        // Only UPLOADED reservations can be validated
        if (reservation.getStatus() != ReservationStatus.UPLOADED) {
            throw new BusinessRuleException(
                    "Cannot validate a reservation with status: " + reservation.getStatus()
                            + ". Expected: UPLOADED");
        }

        // 3. Transition reservation to VALIDATED
        reservation.setStatus(ReservationStatus.VALIDATED);
        Reservation updated = reservationRepositoryPort.update(reservation);

        // 4. Create the associated Order (EPIC-05 logic)
        Order order = createOrderUseCase.createFromReservation(
                updated.getId(),
                updated.getUuid(),
                updated.getClientId(),
                updated.getVendorId(),
                updated.getPaymentMethod(),
                updated.getAccountPreference(),
                updated.getReceiptUrl(),
                updated.getTotal(),
                updated.getDiscount(),
                notes
        );

        if (updated.getRenewalSubscriptionId() != null) {
            completeRenewal(updated, order, callerExternalId);
        }

        // 5. Notify client — PAYMENT_APPROVED (US-035 CA4)
        notifyClient(updated, vendor);

        return updated;
    }

    private void completeRenewal(Reservation reservation, Order order, String callerExternalId) {
        Subscription subscription = subscriptionRepositoryPort.findByInternalId(reservation.getRenewalSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found for renewal reservation: " + reservation.getUuid()));

        renewSubscriptionUseCase.renew(subscription.getUuid(), callerExternalId);

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        Order completed = orderRepositoryPort.save(order);

        orderStatusHistoryPort.record(OrderStatusChange.builder()
                .orderId(completed.getId())
                .oldStatus(oldStatus)
                .newStatus(OrderStatus.COMPLETED)
                .changedBy(callerExternalId)
                .notes("Renewal payment approved")
                .changedAt(java.time.Instant.now())
                .build());
    }

    private void notifyClient(Reservation reservation, Vendor vendor) {
        if (reservation.getClientId() == null) return;

        clientRepositoryPort.findByInternalId(reservation.getClientId())
                .ifPresent(client -> {
                    String payload = String.format(
                            "{\"reservationId\":\"%s\",\"clientName\":\"%s\",\"storeName\":\"%s\",\"total\":\"%s\"}",
                            reservation.getUuid(), client.getName(), vendor.getStoreName(), reservation.getTotal());

                    notificationLogPort.record("PAYMENT_APPROVED", client.getEmail(), payload,
                            "order", reservation.getId(), "approved");
                });
    }
}
