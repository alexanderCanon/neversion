package com.neversion.api.auth.infrastructure.adapters.in.rest.controller;

import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterClientRequest;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterClientResponse;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterVendorRequest;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterVendorResponse;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.CurrentUserResponse;
import com.neversion.api.auth.infrastructure.adapters.in.rest.mapper.CurrentUserResponseMapper;
import com.neversion.api.auth.infrastructure.adapters.in.rest.mapper.RegisterClientRequestMapper;
import com.neversion.api.auth.infrastructure.adapters.in.rest.mapper.RegisterVendorRequestMapper;
import com.neversion.api.user.application.port.in.GetCurrentUserContextUseCase;
import com.neversion.api.user.application.port.in.RegisterClientUseCase;
import com.neversion.api.user.application.port.in.RegisterVendorUseCase;
import com.neversion.api.user.domain.model.CurrentUserContextResult;
import com.neversion.api.user.domain.model.RegisterClientResult;
import com.neversion.api.user.domain.model.RegisterVendorResult;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication and identity management operations.
 * <p>
 * Vendor registration requires SUPER_ADMIN role (US-012).
 * Client registration is a public endpoint (US-013).
 * RBAC is enforced by AuthSecurityConfig.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "Identity management — vendor and client registration")
public class AuthController {

    private final RegisterVendorUseCase registerVendorUseCase;
    private final RegisterClientUseCase registerClientUseCase;
    private final GetCurrentUserContextUseCase getCurrentUserContextUseCase;

    public AuthController(
            RegisterVendorUseCase registerVendorUseCase,
            RegisterClientUseCase registerClientUseCase,
            GetCurrentUserContextUseCase getCurrentUserContextUseCase) {
        this.registerVendorUseCase = registerVendorUseCase;
        this.registerClientUseCase = registerClientUseCase;
        this.getCurrentUserContextUseCase = getCurrentUserContextUseCase;
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get authenticated platform context",
            description = "Resolves the Supabase JWT subject to the internal platform user and vendor context."
    )
    @ApiResponse(responseCode = "200", description = "Authenticated context resolved")
    @ApiResponse(responseCode = "401", description = "No valid JWT provided")
    @ApiResponse(responseCode = "404", description = "Internal user or vendor context not found")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        CurrentUserContextResult result = getCurrentUserContextUseCase.get(jwt.getSubject());
        return ResponseEntity.ok(CurrentUserResponseMapper.toResponse(result));
    }

    /**
     * Registers a new vendor in the platform (US-012).
     * <p>
     * Creates the internal user + vendor records and records a VENDOR_WELCOME
     * notification event. The Supabase Auth account must be created by the frontend
     * before calling this endpoint; the resulting externalId is provided in the request (ADR-09 revised).
     */
    @PostMapping("/vendors")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Register a new vendor (US-012)",
            description = "Creates the platform user (role=VENDOR), the vendor record, " +
                    "and a VENDOR_WELCOME notification event. " +
                    "MANUAL STEP: Create the Supabase Auth account separately."
    )
    @ApiResponse(responseCode = "201", description = "Vendor registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "401", description = "No valid JWT provided")
    @ApiResponse(responseCode = "403", description = "Caller is not SUPER_ADMIN")
    public ResponseEntity<RegisterVendorResponse> registerVendor(
            @Valid @RequestBody RegisterVendorRequest request) {

        RegisterVendorResult result = registerVendorUseCase.register(
                RegisterVendorRequestMapper.toCommand(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegisterVendorRequestMapper.toResponse(result));
    }

    /**
     * Registers a new client on a vendor's store (US-013).
     * <p>
     * This endpoint is PUBLIC — any visitor to a vendor's store can register.
     * Creates the internal user + client records linked to the vendor,
     * and records a CLIENT_REGISTRATION notification event (NFR-05).
     */
    @PostMapping("/clients")
    @Operation(
            summary = "Register a new client (US-013)",
            description = "Public endpoint. Creates the platform user (role=CLIENT), " +
                    "the client record linked to the specified vendor, " +
                    "and a CLIENT_REGISTRATION notification event."
    )
    @ApiResponse(responseCode = "201", description = "Client registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<RegisterClientResponse> registerClient(
            @Valid @RequestBody RegisterClientRequest request) {

        RegisterClientResult result = registerClientUseCase.register(
                RegisterClientRequestMapper.toCommand(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegisterClientRequestMapper.toResponse(result));
    }
}
