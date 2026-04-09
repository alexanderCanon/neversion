package com.neversion.api.order.infrastructure.adapters.in.rest.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.application.port.in.GetOrderUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.infrastructure.adapters.in.rest.dto.OrderResponse;
import com.neversion.api.order.infrastructure.adapters.in.rest.mapper.OrderRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order retrieval for admin and customer")
public class OrderGetController {

    private final GetOrderUseCase getOrderUseCase;
    private final OrderRestMapper orderRestMapper;

    public OrderGetController(GetOrderUseCase getOrderUseCase, OrderRestMapper orderRestMapper) {
        this.getOrderUseCase = getOrderUseCase;
        this.orderRestMapper = orderRestMapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve an order by its UUID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponse> getById(
            @Parameter(description = "Order UUID") @PathVariable UUID id) {

        Order order = getOrderUseCase.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + id));

        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    @GetMapping("/by-reservation/{reservationId}")
    @Operation(summary = "Get order by reservation ID", description = "Retrieve the order linked to a specific reservation")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "No order found for the given reservation")
    public ResponseEntity<OrderResponse> getByReservationId(
            @Parameter(description = "Reservation UUID") @PathVariable UUID reservationId) {

        Order order = getOrderUseCase.getByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for reservation: " + reservationId));

        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }
}
