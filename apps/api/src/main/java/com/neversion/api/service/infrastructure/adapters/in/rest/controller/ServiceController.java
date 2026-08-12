package com.neversion.api.service.infrastructure.adapters.in.rest.controller;

import com.neversion.api.service.application.port.in.ServiceUseCase;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceRequest;
import com.neversion.api.service.infrastructure.adapters.in.rest.dto.ServiceResponse;
import com.neversion.api.service.infrastructure.adapters.in.rest.mapper.ServiceMapper;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the digital service catalog (EPIC-02).
 * <p>
 * Endpoint summary:
 * <ul>
 *   <li>POST   /api/v1/services          — Create service (US-017, VENDOR)</li>
 *   <li>PUT    /api/v1/services/{id}     — Update service (US-018, VENDOR owner)</li>
 *   <li>PATCH  /api/v1/services/{id}/status — Toggle active (US-019, VENDOR owner)</li>
 *   <li>GET    /api/v1/services/vendor/{vendorUuid} — Vendor panel list (US-020, VENDOR)</li>
 *   <li>GET    /api/v1/services/store/{vendorUuid}  — Public store catalog (US-021, public)</li>
 *   <li>GET    /api/v1/services/{id}     — Single service lookup (public)</li>
 *   <li>GET    /api/v1/services          — All services (SUPER_ADMIN)</li>
 *   <li>DELETE /api/v1/services/{id}     — Delete service (VENDOR)</li>
 * </ul>
 */
@RestController
@RequestMapping(value = "/api/v1/services", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Services", description = "Digital service catalog management (Netflix, Spotify, etc.)")
public class ServiceController {

    private final ServiceUseCase serviceUseCase;
    private final ServiceMapper serviceMapper;

    public ServiceController(ServiceUseCase serviceUseCase, ServiceMapper serviceMapper) {
        this.serviceUseCase = serviceUseCase;
        this.serviceMapper = serviceMapper;
    }

    // ─── US-017: Create ──────────────────────────────────────────────────────

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a service (US-017)",
            description = "Creates a new service in the caller vendor's catalog. Name must be unique.")
    @ApiResponse(responseCode = "201", description = "Service created")
    @ApiResponse(responseCode = "400", description = "Validation or duplicate name error")
    @ApiResponse(responseCode = "403", description = "Not a vendor")
    public ResponseEntity<ServiceResponse> create(
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        var service = serviceMapper.toDomain(request);
        var created = serviceUseCase.create(service, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceMapper.toResponse(created));
    }

    // ─── US-018: Update ──────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a service (US-018)",
            description = "Updates all editable fields. Identifiers are immutable. Caller must own the service.")
    @ApiResponse(responseCode = "200", description = "Service updated")
    @ApiResponse(responseCode = "403", description = "Caller does not own this service")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        var updated = serviceUseCase.update(id, serviceMapper.toDomain(request), jwt.getSubject());
        return ResponseEntity.ok(serviceMapper.toResponse(updated));
    }

    // ─── US-019: Toggle status ───────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle service active/inactive (US-019)",
            description = "Activates or deactivates a service. Does not affect existing subscriptions.")
    @ApiResponse(responseCode = "200", description = "Status toggled")
    @ApiResponse(responseCode = "403", description = "Caller does not own this service")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<ServiceResponse> toggleStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        var toggled = serviceUseCase.toggleStatus(id, jwt.getSubject());
        return ResponseEntity.ok(serviceMapper.toResponse(toggled));
    }

    // ─── US-020: Vendor panel list ───────────────────────────────────────────

    @GetMapping("/vendor")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List vendor services — panel view (US-020)",
            description = "Returns all services (active and inactive) for the authenticated vendor.")
    @ApiResponse(responseCode = "200", description = "Service list")
    public ResponseEntity<List<ServiceResponse>> listVendorServices(
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal Jwt jwt) {

        var services = serviceUseCase.listByVendor(category, isActive, jwt.getSubject());
        return ResponseEntity.ok(services.stream().map(serviceMapper::toResponse).toList());
    }

    @GetMapping("/vendor/{vendorUuid}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List vendor services by vendor UUID — panel view (US-020, Legacy)",
            description = "Returns all services (active and inactive) for the given vendor. " +
                    "Supports optional filters: category and isActive.")
    @ApiResponse(responseCode = "200", description = "Service list")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<List<ServiceResponse>> listByVendor(
            @PathVariable UUID vendorUuid,
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal Jwt jwt) {

        var services = serviceUseCase.listByVendor(vendorUuid, category, isActive, jwt.getSubject());
        return ResponseEntity.ok(services.stream().map(serviceMapper::toResponse).toList());
    }


    // ─── US-021: Public store catalog ────────────────────────────────────────

    @GetMapping("/store/{vendorUuid}")
    @Operation(summary = "Public store catalog — active services only (US-021)",
            description = "Returns only active services for the given vendor. No authentication required.")
    @ApiResponse(responseCode = "200", description = "Active service catalog")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<List<ServiceResponse>> listActive(@PathVariable UUID vendorUuid) {
        var services = serviceUseCase.listActive(vendorUuid);
        return ResponseEntity.ok(services.stream().map(serviceMapper::toResponse).toList());
    }

    // ─── Generic / admin endpoints ───────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get service by UUID")
    @ApiResponse(responseCode = "200", description = "Service found")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<ServiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceMapper.toResponse(serviceUseCase.getById(id)));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all services (SUPER_ADMIN)")
    @ApiResponse(responseCode = "200", description = "Full service catalog")
    public ResponseEntity<List<ServiceResponse>> list() {
        return ResponseEntity.ok(
                serviceUseCase.getAll().stream().map(serviceMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a service")
    @ApiResponse(responseCode = "204", description = "Service deleted")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        serviceUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
