package com.neversion.api.order.infrastructure.adapters.in.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.application.port.in.GetOrderUseCase;
import com.neversion.api.order.application.port.in.ListOrdersUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.order.infrastructure.adapters.in.rest.dto.OrderDetailResponse;
import com.neversion.api.order.infrastructure.adapters.in.rest.dto.OrderResponse;
import com.neversion.api.order.infrastructure.adapters.in.rest.dto.StatusChangeResponse;
import com.neversion.api.order.infrastructure.adapters.in.rest.mapper.OrderRestMapper;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.mapper.ReservationRestMapper;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Order retrieval for admin and customer")
public class OrderGetController {

    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final OrderRestMapper orderRestMapper;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final ReservationRestMapper reservationRestMapper;
    private final OrderStatusHistoryPort orderStatusHistoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public OrderGetController(GetOrderUseCase getOrderUseCase,
            ListOrdersUseCase listOrdersUseCase,
            OrderRestMapper orderRestMapper,
            ReservationRepositoryPort reservationRepositoryPort,
            ReservationRestMapper reservationRestMapper,
            OrderStatusHistoryPort orderStatusHistoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ClientRepositoryPort clientRepositoryPort) {
        this.getOrderUseCase = getOrderUseCase;
        this.listOrdersUseCase = listOrdersUseCase;
        this.orderRestMapper = orderRestMapper;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.reservationRestMapper = reservationRestMapper;
        this.orderStatusHistoryPort = orderStatusHistoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    // ── US-038: Order Detail ────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail", description = "US-038: Full order detail with reservation origin data and status history. Ownership check enforced.")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "403", description = "Caller does not own this order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderDetailResponse> getById(
            @Parameter(description = "Order UUID") @PathVariable UUID id,
            JwtAuthenticationToken token) {

        Order order = getOrderUseCase.getByUuid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Ownership check (US-038 CA4)
        verifyOwnership(order, extractExternalId(token));

        // Load reservation origin data (US-038 CA1)
        Reservation reservation = null;
        if (order.getReservationId() != null) {
            reservation = reservationRepositoryPort.findById(order.getReservationId()).orElse(null);
        }

        // Resolve client identity for vendor visibility
        String clientName = null;
        String clientEmail = null;
        if (reservation != null && reservation.getClientId() != null) {
            var clientOpt = clientRepositoryPort.findByInternalId(reservation.getClientId());
            if (clientOpt.isPresent()) {
                var client = clientOpt.get();
                clientName = client.getName();
                clientEmail = client.getEmail();
            }
        }

        // Load status history (US-038 CA3)
        List<StatusChangeResponse> history = orderStatusHistoryPort.findByOrderId(order.getId())
                .stream()
                .map(this::toStatusChangeResponse)
                .toList();

        OrderDetailResponse detail = OrderDetailResponse.builder()
                .id(order.getUuid())
                .reservationId(order.getReservationUuid())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .total(order.getTotal())
                .discount(order.getDiscount())
                .notes(order.getNotes())
                .receiptUrl(order.getReceiptUrl())
                .approvedAt(order.getApprovedAt())
                .createdAt(order.getCreatedAt())
                .reservation(reservation != null ? reservationRestMapper.toResponse(reservation, clientName, clientEmail) : null)
                .statusHistory(history)
                .build();

        return ResponseEntity.ok(detail);
    }

    @GetMapping("/by-reservation/{reservationId}")
    @Operation(summary = "Get order by reservation ID", description = "Retrieve the order linked to a specific reservation. Ownership check enforced.")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "403", description = "Caller does not own this order")
    @ApiResponse(responseCode = "404", description = "No order found for the given reservation")
    public ResponseEntity<OrderResponse> getByReservationId(
            @Parameter(description = "Reservation internal ID") @PathVariable Long reservationId,
            JwtAuthenticationToken token) {

        Order order = getOrderUseCase.getByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for reservation: " + reservationId));

        verifyOwnership(order, extractExternalId(token));

        return ResponseEntity.ok(orderRestMapper.toResponse(order));
    }

    // ── US-037: List Orders by Vendor ────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List vendor orders", description = "US-037: Retrieve orders for the authenticated vendor. Supports filtering by client and status.")
    @ApiResponse(responseCode = "200", description = "List of orders")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @Parameter(description = "Optional Client UUID filter") @RequestParam(required = false) UUID clientUuid,
            @Parameter(description = "Optional Status filter") @RequestParam(required = false) OrderStatus status,
            JwtAuthenticationToken token) {

        List<Order> orders = listOrdersUseCase.listOrders(clientUuid, status, extractExternalId(token));
        List<OrderResponse> response = orders.stream()
                .map(orderRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vendor/{vendorUuid}")
    @Operation(summary = "List orders by vendor UUID (US-037, Legacy)", description = "US-037: Retrieve orders for a vendor. Supports filtering by client and status. Restricted to the owner vendor.")
    @ApiResponse(responseCode = "200", description = "List of orders")
    @ApiResponse(responseCode = "403", description = "Caller does not own this vendor account")
    @ApiResponse(responseCode = "404", description = "Vendor or Client not found")
    public ResponseEntity<List<OrderResponse>> listByVendor(
            @Parameter(description = "Vendor UUID") @PathVariable UUID vendorUuid,
            @Parameter(description = "Optional Client UUID filter") @RequestParam(required = false) UUID clientUuid,
            @Parameter(description = "Optional Status filter") @RequestParam(required = false) OrderStatus status,
            JwtAuthenticationToken token) {

        List<Order> orders = listOrdersUseCase.listByVendor(vendorUuid, clientUuid, status, extractExternalId(token));
        List<OrderResponse> response = orders.stream()
                .map(orderRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }


    // ── Helpers ──────────────────────────────────────────────────────────────

    private StatusChangeResponse toStatusChangeResponse(OrderStatusChange change) {
        return new StatusChangeResponse(
                change.getOldStatus(),
                change.getNewStatus(),
                change.getChangedBy(),
                change.getNotes(),
                change.getChangedAt());
    }

    /** US-038 CA4: 403 if vendor tries to access an order that doesn't belong to them.
     *  SUPER_ADMIN bypasses tenant check — they have platform-wide read access. */
    private void verifyOwnership(Order order, String callerExternalId) {
        var caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));

        var vendorOpt = vendorRepositoryPort.findByUserId(caller.getId());
        if (vendorOpt.isEmpty()) {
            // No vendor profile found — treat as SUPER_ADMIN with platform-wide access.
            // Any non-SUPER_ADMIN without a vendor profile would have been blocked at the
            // security layer (hasAnyRole("VENDOR","SUPER_ADMIN")).
            return;
        }

        if (!order.getVendorId().equals(vendorOpt.get().getId())) {
            throw new AccessDeniedException("You do not have permission to access this order.");
        }
    }

    /** Extracts the Supabase externalId (sub claim) from the JWT. */
    private String extractExternalId(java.security.Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
