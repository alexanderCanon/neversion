package com.neversion.api.order.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.ReversePointsUseCase;
import com.neversion.api.order.application.port.in.ChangeOrderStatusUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * UC6: Change Order Status — US-038, US-039.
 */
@Service
public class ChangeOrderStatusService implements ChangeOrderStatusUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderStatusHistoryPort orderStatusHistoryPort;
    private final NotificationLogPort notificationLogPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ReversePointsUseCase reversePointsUseCase;

    public ChangeOrderStatusService(
            OrderRepositoryPort orderRepositoryPort,
            OrderStatusHistoryPort orderStatusHistoryPort,
            NotificationLogPort notificationLogPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ReversePointsUseCase reversePointsUseCase) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderStatusHistoryPort = orderStatusHistoryPort;
        this.notificationLogPort = notificationLogPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.reversePointsUseCase = reversePointsUseCase;
    }

    @Override
    @Transactional
    public Order changeStatus(UUID orderUuid, OrderStatus newStatus, String notes, String callerExternalId) {

        // 1. Resolve caller vendor for ownership check
        User caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));
        
        Vendor vendor = vendorRepositoryPort.findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for caller"));

        // 2. Load and validate order
        Order order = orderRepositoryPort.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderUuid));

        // Ownership check
        if (!order.getVendorId().equals(vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this order.");
        }

        // Validate state transition (basic)
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot modify a finalized order (COMPLETED/CANCELLED).");
        }

        // 3. Record status change in audit trail (US-038 CA3)
        OrderStatus oldStatus = order.getStatus();

        order.setStatus(newStatus);
        if (notes != null) {
            order.setNotes(notes);
        }
        Order updated = orderRepositoryPort.save(order);

        orderStatusHistoryPort.record(OrderStatusChange.builder()
                .orderId(updated.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(callerExternalId)
                .notes(notes)
                .changedAt(Instant.now())
                .build());

        // 3b. Reverse earned points if the order is cancelled (loyalty program)
        if (newStatus == OrderStatus.CANCELLED) {
            reversePointsUseCase.reverseForOrder(updated.getId());
        }

        // 4. Notify Client
        notifyClient(updated);

        return updated;
    }

    private void notifyClient(Order order) {
        if (order.getClientId() == null) return;

        clientRepositoryPort.findByInternalId(order.getClientId())
                .ifPresent(client -> {
                    String eventType = order.getStatus() == OrderStatus.COMPLETED 
                            ? "ORDER_COMPLETED" : "ORDER_CANCELLED";
                    
                    String payload = String.format(
                            "{\"orderId\":\"%s\",\"clientName\":\"%s\",\"status\":\"%s\"}",
                            order.getUuid(), client.getName(), order.getStatus());
                    
                    String stage = order.getStatus() == OrderStatus.COMPLETED 
                            ? "completed" : "cancelled";
                    
                    notificationLogPort.record(eventType, client.getEmail(), payload,
                            "order", order.getId(), stage);
                });
    }
}
