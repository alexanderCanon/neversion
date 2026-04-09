package com.neversion.api.client.infrastructure.adapters.in.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientRequest;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.mapper.ClientMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Client (end consumer) management")
public class ClientController {

    private final ClientUseCase clientUseCase;
    private final ClientMapper clientMapper;

    public ClientController(ClientUseCase clientUseCase, ClientMapper clientMapper) {
        this.clientUseCase = clientUseCase;
        this.clientMapper = clientMapper;
    }

    @PostMapping
    @Operation(summary = "Register a client (CU-A02)")
    @ApiResponse(responseCode = "201", description = "Client registered")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.toDomain(request);
        Client created = clientUseCase.create(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by UUID")
    @ApiResponse(responseCode = "200", description = "Client found")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<ClientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientMapper.toResponse(clientUseCase.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List clients", description = "Returns all clients. Filter by name or phone.")
    @ApiResponse(responseCode = "200", description = "Client list")
    public ResponseEntity<List<ClientResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone) {

        List<Client> clients;
        if (name != null && !name.isBlank()) {
            clients = clientUseCase.getByName(name);
        } else if (phone != null && !phone.isBlank()) {
            clients = clientUseCase.getByPhone(phone);
        } else {
            clients = clientUseCase.getAll();
        }

        return ResponseEntity.ok(clients.stream().map(clientMapper::toResponse).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client data")
    @ApiResponse(responseCode = "200", description = "Client updated")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<ClientResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRequest request) {
        Client updated = clientUseCase.update(id, clientMapper.toDomain(request));
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client")
    @ApiResponse(responseCode = "204", description = "Client deleted")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
