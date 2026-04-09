package com.neversion.api.reservation.infrastructure.adapters.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.reservation.application.port.in.CreateReservationUseCase;
import com.neversion.api.reservation.application.port.in.ReservationItemCommand;
import com.neversion.api.reservation.application.port.in.UploadReceiptUseCase;
import com.neversion.api.reservation.application.port.in.ValidateReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ReservationRequest;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ReservationResponse;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.UploadReceiptRequest;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ValidateReservationRequest;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.mapper.ReservationRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Reservation management for client purchases")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final UploadReceiptUseCase uploadReceiptUseCase;
    private final ValidateReservationUseCase validateReservationUseCase;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ReservationRestMapper reservationRestMapper;

    public ReservationController(
            CreateReservationUseCase createReservationUseCase,
            UploadReceiptUseCase uploadReceiptUseCase,
            ValidateReservationUseCase validateReservationUseCase,
            ReservationRepositoryPort reservationRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ReservationRestMapper reservationRestMapper) {
        this.createReservationUseCase = createReservationUseCase;
        this.uploadReceiptUseCase = uploadReceiptUseCase;
        this.validateReservationUseCase = validateReservationUseCase;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.reservationRestMapper = reservationRestMapper;
    }

    // ── UC1: Create Reservation (Checkout) ──────────────────────────────

    @PostMapping
    @Operation(summary = "Create a reservation", description = "UC1: Create a new reservation with items. clientId is optional and can be attached later.")
    @ApiResponse(responseCode = "201", description = "Reservation created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {

        List<ReservationItemCommand> items = reservationRestMapper.toItemCommands(request.items());
        Reservation reservation = createReservationUseCase.create(request.clientId(), items);
        ReservationResponse response = reservationRestMapper.toResponse(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET: List all reservations (Admin) ──────────────────────────────

    @GetMapping
    @Operation(summary = "List all reservations", description = "Admin: Retrieve all reservations, optionally filtered by status.")
    @ApiResponse(responseCode = "200", description = "List of reservations")
    public ResponseEntity<List<ReservationResponse>> listReservations(
            @Parameter(description = "Filter by reservation status (e.g. PENDING, UPLOADED, VALIDATED)") @RequestParam(required = false) ReservationStatus status) {

        List<Reservation> reservations = (status != null)
                ? reservationRepositoryPort.findByStatus(status)
                : reservationRepositoryPort.findAll();

        List<ReservationResponse> responses = reservations.stream()
                .map(reservationRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // ── GET: Retrieve reservation by ID ─────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a reservation by ID", description = "Retrieve reservation details including status, total, and line items.")
    @ApiResponse(responseCode = "200", description = "Reservation found")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> getReservation(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id) {

        Reservation reservation = reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));

        return ResponseEntity.ok(reservationRestMapper.toResponse(reservation));
    }

    // ── UC2: Upload Receipt (Report Payment) ────────────────────────────

    @PutMapping("/{id}/receipt")
    @Operation(summary = "Upload payment receipt", description = "UC2: Customer uploads receipt URL. Transitions PENDING → UPLOADED.")
    @ApiResponse(responseCode = "200", description = "Receipt uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status or duplicate receipt URL (BR-05)")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> uploadReceipt(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id,
            @Valid @RequestBody UploadReceiptRequest request) {

        Reservation reservation = uploadReceiptUseCase.uploadReceipt(id, request.receiptUrl());
        return ResponseEntity.ok(reservationRestMapper.toResponse(reservation));
    }

    // ── UC3: Validate Payment (Admin Only) ──────────────────────────────

    @PutMapping("/{id}/validate")
    @Operation(summary = "Validate payment and create order", description = "UC3: Admin validates payment receipt. Transitions UPLOADED → VALIDATED and creates an Order.")
    @ApiResponse(responseCode = "200", description = "Payment validated, order created")
    @ApiResponse(responseCode = "400", description = "Invalid status (not UPLOADED)")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> validateReservation(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id,
            @RequestBody(required = false) ValidateReservationRequest request) {

        String notes = request != null ? request.notes() : null;
        Reservation reservation = validateReservationUseCase.validate(id, notes);
        return ResponseEntity.ok(reservationRestMapper.toResponse(reservation));
    }

    // ── Cancel Reservation ──────────────────────────────────────────────

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Admin or customer manually cancels a reservation. Only PENDING or UPLOADED can be cancelled.")
    @ApiResponse(responseCode = "200", description = "Reservation cancelled")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be cancelled in its current status")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id) {

        Reservation reservation = reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.UPLOADED) {
            throw new BusinessRuleException(
                    "Only PENDING or UPLOADED reservations can be cancelled. Current status: "
                            + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updated = reservationRepositoryPort.update(reservation);
        return ResponseEntity.ok(reservationRestMapper.toResponse(updated));
    }

    // ── Attach Client ───────────────────────────────────────────────

    @PutMapping("/{id}/client")
    @Operation(summary = "Attach client to reservation", description = "Link an existing client to a reservation that was created without one.")
    @ApiResponse(responseCode = "200", description = "Client attached")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> attachClient(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id,
            @RequestParam UUID clientId) {

        Reservation reservation = reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));

        // Resolve internal Client ID
        Long internalClientId = clientRepositoryPort.findById(clientId)
                .map(Client::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));

        reservation.setClientId(internalClientId);
        reservation.setClientUuid(clientId);
        Reservation updated = reservationRepositoryPort.update(reservation);
        return ResponseEntity.ok(reservationRestMapper.toResponse(updated));
    }
}
