package com.neversion.api.order.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderService unit tests")
class GetOrderServiceUT {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    private GetOrderService getOrderService;

    @BeforeEach
    void setUp() {
        getOrderService = new GetOrderService(orderRepositoryPort);
    }

    @Test
    @DisplayName("getByUuid - should return matching order when found")
    void getByUuid_shouldReturnOrder_whenFound() {
        UUID uuid = UUID.randomUUID();
        Order order = Order.builder().id(1L).uuid(uuid).build();
        when(orderRepositoryPort.findByUuid(uuid)).thenReturn(Optional.of(order));

        Optional<Order> result = getOrderService.getByUuid(uuid);

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("getByUuid - should return empty optional when not found")
    void getByUuid_shouldReturnEmpty_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(orderRepositoryPort.findByUuid(uuid)).thenReturn(Optional.empty());

        Optional<Order> result = getOrderService.getByUuid(uuid);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getByReservationId - should return matching order when found")
    void getByReservationId_shouldReturnOrder_whenFound() {
        Long reservationId = 99L;
        Order order = Order.builder().id(1L).reservationId(reservationId).build();
        when(orderRepositoryPort.findByReservationId(reservationId)).thenReturn(Optional.of(order));

        Optional<Order> result = getOrderService.getByReservationId(reservationId);

        assertThat(result).isPresent();
        assertThat(result.get().getReservationId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("getByReservationId - should return empty optional when not found")
    void getByReservationId_shouldReturnEmpty_whenNotFound() {
        Long reservationId = 99L;
        when(orderRepositoryPort.findByReservationId(reservationId)).thenReturn(Optional.empty());

        Optional<Order> result = getOrderService.getByReservationId(reservationId);

        assertThat(result).isEmpty();
    }
}
