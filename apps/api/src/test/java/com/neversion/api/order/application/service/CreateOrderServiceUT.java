package com.neversion.api.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.loyalty.application.port.in.EarnPointsUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.shared.domain.model.enums.AccountPreference;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderService unit tests — US-035")
class CreateOrderServiceUT {

    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private OrderStatusHistoryPort orderStatusHistoryPort;
    @Mock private EarnPointsUseCase earnPointsUseCase;

    private CreateOrderService createOrderService;

    @BeforeEach
    void setUp() {
        createOrderService = new CreateOrderService(orderRepositoryPort, orderStatusHistoryPort, earnPointsUseCase);
    }

    @Test
    @DisplayName("createFromReservation - should create order with all EPIC-05 metadata and record audit")
    void createFromReservation_shouldPopulateMetadata() {
        // Given
        Long reservationId = 1L;
        Long clientId = 10L;
        Long vendorId = 5L;
        String paymentMethod = "transferencia";
        String notes = "Validated by vendor";

        when(orderRepositoryPort.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    o.setId(100L);
                    return o;
                });

        // When
        java.math.BigDecimal total = new java.math.BigDecimal("150.00");
        java.math.BigDecimal discount = new java.math.BigDecimal("3.00");
        Order result = createOrderService.createFromReservation(
                reservationId, UUID.randomUUID(), clientId, vendorId, paymentMethod,
                null, "http://receipt.url", total, discount, notes);

        // Then
        assertThat(result.getReservationId()).isEqualTo(reservationId);
        assertThat(result.getReservationUuid()).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);
        assertThat(result.getVendorId()).isEqualTo(vendorId);
        assertThat(result.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(result.getAccountPreference()).isNull();
        assertThat(result.getReceiptUrl()).isEqualTo("http://receipt.url");
        assertThat(result.getTotal()).isEqualTo(total);
        assertThat(result.getDiscount()).isEqualTo(discount);
        assertThat(result.getNotes()).isEqualTo(notes);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.VALIDATED);
        assertThat(result.getApprovedAt()).isNotNull();

        verify(orderRepositoryPort).save(any(Order.class));
        verify(orderStatusHistoryPort).record(any(OrderStatusChange.class));
    }
}
