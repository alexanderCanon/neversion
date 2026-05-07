package com.neversion.api.order.infrastructure.adapters.in.rest.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.order.application.port.in.ChangeOrderStatusUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.infrastructure.adapters.in.rest.dto.OrderResponse;
import com.neversion.api.order.infrastructure.adapters.in.rest.mapper.OrderRestMapper;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ValidateReservationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Order retrieval for admin and customer")
public class OrderController {

    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final OrderRestMapper orderRestMapper;

    public OrderController(ChangeOrderStatusUseCase changeOrderStatusUseCase, OrderRestMapper orderRestMapper) {
        this.changeOrderStatusUseCase = changeOrderStatusUseCase;
        this.orderRestMapper = orderRestMapper;
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete order", description = "US-039: Transition order to COMPLETED status. Restricted to the owner vendor.")
    @ApiResponse(responseCode = "200", description = "Order completed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid transition")
    @ApiResponse(responseCode = "403", description = "Caller does not own this order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Order UUID") @PathVariable UUID id,
            @RequestBody(required = false) ValidateReservationRequest request,
            JwtAuthenticationToken token) {

        String notes = request != null ? request.notes() : null;
        Order order = changeOrderStatusUseCase.changeStatus(id, OrderStatus.COMPLETED, notes, extractExternalId(token));
        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "US-038: Transition order to CANCELLED status. Restricted to the owner vendor.")
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Invalid transition")
    @ApiResponse(responseCode = "403", description = "Caller does not own this order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order UUID") @PathVariable UUID id,
            @RequestBody(required = false) ValidateReservationRequest request,
            JwtAuthenticationToken token) {

        String notes = request != null ? request.notes() : null;
        Order order = changeOrderStatusUseCase.changeStatus(id, OrderStatus.CANCELLED, notes, extractExternalId(token));
        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractExternalId(java.security.Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
