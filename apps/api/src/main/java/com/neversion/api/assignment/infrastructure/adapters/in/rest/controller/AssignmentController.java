package com.neversion.api.assignment.infrastructure.adapters.in.rest.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.assignment.application.port.in.ConfirmAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.ManualAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.SuggestAssignmentUseCase;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.ConfirmAssignmentRequest;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.ConfirmAssignmentResponse;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.ManualAssignmentRequest;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.ManualAssignmentResponse;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.SuggestAssignmentResponse;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.mapper.AssignmentRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/assignments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Assignments", description = "Access assignment and delivery workflow (EPIC-06)")
public class AssignmentController {

    private final SuggestAssignmentUseCase suggestAssignmentUseCase;
    private final ConfirmAssignmentUseCase confirmAssignmentUseCase;
    private final ManualAssignmentUseCase manualAssignmentUseCase;
    private final AssignmentRestMapper assignmentRestMapper;

    public AssignmentController(
            SuggestAssignmentUseCase suggestAssignmentUseCase,
            ConfirmAssignmentUseCase confirmAssignmentUseCase,
            ManualAssignmentUseCase manualAssignmentUseCase,
            AssignmentRestMapper assignmentRestMapper) {
        this.suggestAssignmentUseCase = suggestAssignmentUseCase;
        this.confirmAssignmentUseCase = confirmAssignmentUseCase;
        this.manualAssignmentUseCase = manualAssignmentUseCase;
        this.assignmentRestMapper = assignmentRestMapper;
    }

    @GetMapping("/suggest/{orderId}")
    @Operation(summary = "Suggest assignment (US-039)",
            description = "Suggests an available profile for a validated single-item order.")
    @ApiResponse(responseCode = "200", description = "Suggestion evaluated")
    @ApiResponse(responseCode = "400", description = "Order is not assignable")
    @ApiResponse(responseCode = "403", description = "Caller does not own this order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<SuggestAssignmentResponse> suggest(
            @Parameter(description = "Order UUID") @PathVariable UUID orderId,
            JwtAuthenticationToken token) {
        var suggestion = suggestAssignmentUseCase.suggest(orderId, extractExternalId(token));
        return ResponseEntity.ok(assignmentRestMapper.toResponse(suggestion));
    }

    @PostMapping("/confirm/{orderId}")
    @Operation(summary = "Confirm assignment (US-040)",
            description = "Confirms a vendor-selected profile and completes the order.")
    @ApiResponse(responseCode = "201", description = "Assignment confirmed")
    @ApiResponse(responseCode = "400", description = "Profile or order is not assignable")
    @ApiResponse(responseCode = "403", description = "Caller does not own this order or profile")
    @ApiResponse(responseCode = "404", description = "Order or profile not found")
    @ApiResponse(responseCode = "409", description = "Assignment already confirmed")
    public ResponseEntity<ConfirmAssignmentResponse> confirm(
            @Parameter(description = "Order UUID") @PathVariable UUID orderId,
            @Valid @RequestBody ConfirmAssignmentRequest request,
            JwtAuthenticationToken token) {
        var result = confirmAssignmentUseCase.confirm(orderId, request.profileId(), extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentRestMapper.toConfirmResponse(result));
    }

    @PostMapping("/manual")
    @Operation(summary = "Create manual assignment (US-042)",
            description = "Creates a subscription without reservation or order, for external sales or migration support.")
    @ApiResponse(responseCode = "201", description = "Manual assignment created")
    @ApiResponse(responseCode = "400", description = "Invalid profile, service, or date range")
    @ApiResponse(responseCode = "403", description = "Caller does not own the selected resources")
    @ApiResponse(responseCode = "404", description = "Client, service, or profile not found")
    @ApiResponse(responseCode = "409", description = "Profile already has an active subscription")
    public ResponseEntity<ManualAssignmentResponse> manual(
            @Valid @RequestBody ManualAssignmentRequest request,
            JwtAuthenticationToken token) {
        var result = manualAssignmentUseCase.assign(
                request.clientId(),
                request.serviceId(),
                request.profileId(),
                request.startDate(),
                request.endDate(),
                extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentRestMapper.toManualResponse(result));
    }

    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
