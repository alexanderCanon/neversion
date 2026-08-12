package com.neversion.api.client.infrastructure.adapters.in.rest.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.client.application.port.in.ClientUseCase;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientAccessDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientOrderHistoryDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientReservationStatusDetail;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientAccessResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientDeletionCheckResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientDetailResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientOrderHistoryResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientReservationStatusResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientRequest;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.UpdateClientRequest;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.UpdateClientProfileRequest;
import com.neversion.api.client.infrastructure.adapters.in.rest.mapper.ClientMapper;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clients", description = "Client (end consumer) management — EPIC-04")
public class ClientController {

    private final ClientUseCase clientUseCase;
    private final ClientMapper clientMapper;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    public ClientController(ClientUseCase clientUseCase, ClientMapper clientMapper,
            SubscriptionRepositoryPort subscriptionRepositoryPort) {
        this.clientUseCase = clientUseCase;
        this.clientMapper = clientMapper;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
    }

    // ── US-029 — Listar clientes del vendor ────────────────────────────────

    @GetMapping
    @Operation(summary = "List vendor clients (US-029)",
            description = "Returns all clients of the authenticated vendor with optional filters.")
    @ApiResponse(responseCode = "200", description = "Client list")
    public ResponseEntity<List<ClientResponse>> listClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            JwtAuthenticationToken token) {

        List<Client> clients = clientUseCase.listClients(
                name, phone, email, extractExternalId(token));

        List<ClientResponse> response = clients.stream()
                .map(c -> {
                    long activeCount = subscriptionRepositoryPort.findByClientId(c.getId())
                            .stream()
                            .filter(s -> SubStatus.ACTIVE.equals(s.getStatus()))
                            .count();
                    return clientMapper.toResponse(c, activeCount);
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vendor/{vendorUuid}")
    @Operation(summary = "List vendor clients by vendor UUID (US-029, Legacy)",
            description = "Returns all clients of the vendor with optional filters. "
                    + "Only the authenticated vendor can list their own clients.")
    @ApiResponse(responseCode = "200", description = "Client list")
    @ApiResponse(responseCode = "403", description = "Caller does not own this vendor")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<List<ClientResponse>> listByVendor(
            @PathVariable UUID vendorUuid,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            JwtAuthenticationToken token) {

        List<Client> clients = clientUseCase.listByVendor(
                vendorUuid, name, phone, email, extractExternalId(token));

        List<ClientResponse> response = clients.stream()
                .map(c -> {
                    long activeCount = subscriptionRepositoryPort.findByClientId(c.getId())
                            .stream()
                            .filter(s -> SubStatus.ACTIVE.equals(s.getStatus()))
                            .count();
                    return clientMapper.toResponse(c, activeCount);
                })
                .toList();

        return ResponseEntity.ok(response);
    }


    // ── US-030 — Detalle de cliente ────────────────────────────────────────

    @GetMapping("/{id}/detail")
    @Operation(summary = "Client detail with subscriptions + orders (US-030)",
            description = "Returns full client data, active subscriptions and order history. "
                    + "403 if the caller does not own the client.")
    @ApiResponse(responseCode = "200", description = "Client detail")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<ClientDetailResponse> getDetail(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(clientMapper.toDetailResponse(
                clientUseCase.getDetail(id, extractExternalId(token))));
    }

    @GetMapping("/me/accesses")
    @Operation(summary = "Get my access credentials (US-041)",
            description = "Returns full access credentials (active subscriptions) for the authenticated client. "
                    + "The client is resolved automatically from the JWT.")
    @ApiResponse(responseCode = "200", description = "Access credentials list")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<List<ClientAccessResponse>> getMyAccesses(
            JwtAuthenticationToken token) {
        List<ClientAccessDetail> details = clientUseCase.getMyAccesses(extractExternalId(token));
        List<ClientAccessResponse> response = details.stream()
                .map(clientMapper::toAccessResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/orders")
    @Operation(summary = "Get my order history (US-059)",
            description = "Returns order history for the authenticated client. "
                    + "The client is resolved from the JWT and no client ID is accepted in the request.")
    @ApiResponse(responseCode = "200", description = "Client order history")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Only clients can access this endpoint")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<List<ClientOrderHistoryResponse>> getMyOrders(
            JwtAuthenticationToken token) {
        List<ClientOrderHistoryDetail> details = clientUseCase.getMyOrders(extractExternalId(token));
        List<ClientOrderHistoryResponse> response = details.stream()
                .map(clientMapper::toOrderHistoryResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/reservations")
    @Operation(summary = "Get my reservation and receipt statuses (US-060)",
            description = "Returns reservation statuses for the authenticated client, including rejection notes. "
                    + "The client is resolved from the JWT and no client ID is accepted in the request.")
    @ApiResponse(responseCode = "200", description = "Client reservation status history")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Only clients can access this endpoint")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<List<ClientReservationStatusResponse>> getMyReservations(
            JwtAuthenticationToken token) {
        List<ClientReservationStatusDetail> details =
                clientUseCase.getMyReservations(extractExternalId(token));
        List<ClientReservationStatusResponse> response = details.stream()
                .map(clientMapper::toReservationStatusResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my client profile (US-062)",
            description = "Returns the authenticated client's basic profile. "
                    + "Email is returned for display but remains immutable.")
    @ApiResponse(responseCode = "200", description = "Client profile")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Only clients can access this endpoint")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<ClientResponse> getMyProfile(JwtAuthenticationToken token) {
        Client client = clientUseCase.getMyProfile(extractExternalId(token));
        return ResponseEntity.ok(clientMapper.toResponse(client));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my client profile (US-062)",
            description = "Updates only name and phone for the authenticated client. "
                    + "Email is not accepted in the request and remains immutable.")
    @ApiResponse(responseCode = "200", description = "Client profile updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Only clients can access this endpoint")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<ClientResponse> updateMyProfile(
            @Valid @RequestBody UpdateClientProfileRequest request,
            JwtAuthenticationToken token) {
        Client updated = clientUseCase.updateMyProfile(
                request.name(), request.phone(), extractExternalId(token));
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    // ── US-031 — Crear cliente manual ──────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create client manually (US-031)",
            description = "Creates a client linked to the authenticated vendor. "
                    + "400 if email already exists. Logs CLIENT_WELCOME notification.")
    @ApiResponse(responseCode = "201", description = "Client created")
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate email")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ClientResponse> create(
            @Valid @RequestBody ClientRequest request,
            JwtAuthenticationToken token) {
        Client client = clientMapper.toDomain(request);
        Client created = clientUseCase.createForVendor(client, extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED).body(clientMapper.toResponse(created));
    }

    // ── US-032 — Editar datos básicos ──────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update basic client data (US-032)",
            description = "Updates name, phone, notes. email is immutable (BR-US032-01). "
                    + "403 if caller does not own the client.")
    @ApiResponse(responseCode = "200", description = "Client updated")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<ClientResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequest request,
            JwtAuthenticationToken token) {
        Client updated = clientUseCase.update(
                id, request.name(), request.phone(), request.notes(),
                extractExternalId(token));
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    // ── Generic endpoints (legacy) ─────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get client by UUID")
    @ApiResponse(responseCode = "200", description = "Client found")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<ClientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientMapper.toResponse(clientUseCase.getById(id)));
    }

    @GetMapping("/{id}/deletion-check")
    @Operation(summary = "Check related data before deleting a client",
            description = "Returns counts of active subscriptions, pending reservations and total orders "
                    + "linked to the client so the vendor can review before confirming deletion. "
                    + "403 if the caller does not own the client.")
    @ApiResponse(responseCode = "200", description = "Deletion check result")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<ClientDeletionCheckResponse> checkDeletion(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        ClientUseCase.DeletionCheck check = clientUseCase.checkDeletion(id, extractExternalId(token));
        return ResponseEntity.ok(new ClientDeletionCheckResponse(
                check.activeSubscriptions(),
                check.pendingReservations(),
                check.totalOrders(),
                check.hasRelatedData()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a client",
            description = "Sets deleted_at on the client record (logical deletion). "
                    + "403 if the caller does not own the client.")
    @ApiResponse(responseCode = "204", description = "Client deleted")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        clientUseCase.delete(id, extractExternalId(token));
        return ResponseEntity.noContent().build();
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    /** Extracts the Supabase externalId (sub claim) from the JWT. */
    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
