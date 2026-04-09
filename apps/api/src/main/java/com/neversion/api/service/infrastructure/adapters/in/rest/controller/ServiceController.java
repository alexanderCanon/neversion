package com.neversion.api.service.infrastructure.adapters.in.rest.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.service.application.port.in.ServiceUseCase;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceRequest;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceResponse;
import com.neversion.api.service.infrastructure.adapters.in.rest.mapper.ServiceMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Digital service catalog management (Netflix, Spotify, etc.)")
public class ServiceController {

    private final ServiceUseCase serviceUseCase;
    private final ServiceMapper serviceMapper;

    public ServiceController(ServiceUseCase serviceUseCase, ServiceMapper serviceMapper) {
        this.serviceUseCase = serviceUseCase;
        this.serviceMapper = serviceMapper;
    }

    @PostMapping
    @Operation(summary = "Create a service",
            description = "Adds a new digital service to the catalog. Name must be unique.")
    @ApiResponse(responseCode = "201", description = "Service created")
    @ApiResponse(responseCode = "400", description = "Validation or duplicate name error")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        var service = serviceMapper.toDomain(request);
        var created = serviceUseCase.create(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by UUID")
    @ApiResponse(responseCode = "200", description = "Service found")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<ServiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceMapper.toResponse(serviceUseCase.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List all services")
    @ApiResponse(responseCode = "200", description = "Service catalog")
    public ResponseEntity<List<ServiceResponse>> list() {
        return ResponseEntity.ok(
                serviceUseCase.getAll().stream().map(serviceMapper::toResponse).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service (name, maxProfiles, details)")
    @ApiResponse(responseCode = "200", description = "Service updated")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceRequest request) {
        var updated = serviceUseCase.update(id, serviceMapper.toDomain(request));
        return ResponseEntity.ok(serviceMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service")
    @ApiResponse(responseCode = "204", description = "Service deleted")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
