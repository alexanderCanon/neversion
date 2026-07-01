package com.neversion.api.order.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.loyalty.application.port.in.EarnPointsUseCase;
import com.neversion.api.order.application.port.in.CreateOrderUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;

import com.neversion.api.shared.domain.model.enums.AccountPreference;

@Service
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderStatusHistoryPort orderStatusHistoryPort;
    private final EarnPointsUseCase earnPointsUseCase;

    public CreateOrderService(OrderRepositoryPort orderRepositoryPort,
            OrderStatusHistoryPort orderStatusHistoryPort,
            EarnPointsUseCase earnPointsUseCase) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderStatusHistoryPort = orderStatusHistoryPort;
        this.earnPointsUseCase = earnPointsUseCase;
    }

    @Override
    @Transactional
    public Order createFromReservation(Long reservationId, UUID reservationUuid, Long clientId,
            Long vendorId, String paymentMethod, AccountPreference accountPreference, String receiptUrl,
            BigDecimal total, BigDecimal discount, String notes) {

        Order order = Order.builder()
                .reservationId(reservationId)
                .reservationUuid(reservationUuid)
                .clientId(clientId)
                .vendorId(vendorId)
                .status(OrderStatus.VALIDATED)
                .paymentMethod(paymentMethod)
                .accountPreference(accountPreference)
                .receiptUrl(receiptUrl)
                .total(total)
                .discount(discount)
                .notes(notes)
                .approvedAt(Instant.now())
                .build();

        Order saved = orderRepositoryPort.save(order);

        // US-038 CA3: Record initial status in audit trail
        orderStatusHistoryPort.record(OrderStatusChange.builder()
                .orderId(saved.getId())
                .oldStatus(null)
                .newStatus(OrderStatus.VALIDATED)
                .changedBy("system")
                .notes("Order created from approved reservation")
                .changedAt(Instant.now())
                .build());

        // Credit loyalty points based on the vendor's rewards_cfg (no-op if disabled)
        earnPointsUseCase.earnForOrder(saved.getId(), clientId, vendorId, total);

        return saved;
    }
}
