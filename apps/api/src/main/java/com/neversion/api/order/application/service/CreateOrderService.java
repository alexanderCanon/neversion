package com.neversion.api.order.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.order.application.port.in.CreateOrderUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;

/**
 * Creates an Order record linked to a validated reservation.
 */
@Service
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public CreateOrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    @Transactional
    public Order createFromReservation(UUID reservationId, String notes) {
        Order order = Order.builder()
                .reservationId(reservationId)
                .status(OrderStatus.VALIDATED)
                .notes(notes)
                .build();

        return orderRepositoryPort.save(order);
    }
}
