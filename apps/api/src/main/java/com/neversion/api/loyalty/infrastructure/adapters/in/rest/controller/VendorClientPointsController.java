package com.neversion.api.loyalty.infrastructure.adapters.in.rest.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.loyalty.application.port.in.AdjustPointsUseCase;
import com.neversion.api.loyalty.application.port.in.GetPointsSummaryUseCase;
import com.neversion.api.loyalty.application.port.in.ListPointsMovementsUseCase;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.AdjustPointsRequest;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsMovementResponse;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsMovementsPageResponse;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsSummaryResponse;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.mapper.PointsRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Vendor panel endpoints for viewing and manually adjusting a client's loyalty points.
 * Ownership is enforced: the caller must be the vendor owning the target client.
 */
@RestController
@RequestMapping(value = "/api/v1/vendor/clients/{clientUuid}/points", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Loyalty Points", description = "Loyalty points management and history")
public class VendorClientPointsController {

    private final GetPointsSummaryUseCase getPointsSummaryUseCase;
    private final ListPointsMovementsUseCase listPointsMovementsUseCase;
    private final AdjustPointsUseCase adjustPointsUseCase;
    private final PointsRestMapper pointsRestMapper;

    public VendorClientPointsController(
            GetPointsSummaryUseCase getPointsSummaryUseCase,
            ListPointsMovementsUseCase listPointsMovementsUseCase,
            AdjustPointsUseCase adjustPointsUseCase,
            PointsRestMapper pointsRestMapper) {
        this.getPointsSummaryUseCase = getPointsSummaryUseCase;
        this.listPointsMovementsUseCase = listPointsMovementsUseCase;
        this.adjustPointsUseCase = adjustPointsUseCase;
        this.pointsRestMapper = pointsRestMapper;
    }

    @GetMapping
    @Operation(summary = "Get a client's loyalty points balance",
            description = "Returns the points summary for a client owned by the authenticated vendor.")
    @ApiResponse(responseCode = "200", description = "Points summary")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<PointsSummaryResponse> getSummary(
            @PathVariable UUID clientUuid,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(pointsRestMapper.toResponse(
                getPointsSummaryUseCase.getForClientAsVendor(extractExternalId(token), clientUuid)));
    }

    @GetMapping("/movements")
    @Operation(summary = "Get a client's loyalty points movement history",
            description = "Returns a paginated list of points ledger movements for a client owned by the vendor.")
    @ApiResponse(responseCode = "200", description = "Points movements")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<PointsMovementsPageResponse> getMovements(
            @PathVariable UUID clientUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            JwtAuthenticationToken token) {

        Pageable pageable = PageRequest.of(page, size);
        var result = listPointsMovementsUseCase.listForClientAsVendor(
                extractExternalId(token), clientUuid, pageable);
        return ResponseEntity.ok(new PointsMovementsPageResponse(
                pointsRestMapper.toResponseList(result), result.totalElements()));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Manually adjust a client's loyalty points",
            description = "Credits or debits points for a client owned by the authenticated vendor. "
                    + "A reason (notes) is mandatory. Debits are rejected if they would exceed the available balance.")
    @ApiResponse(responseCode = "200", description = "Adjustment recorded")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance")
    @ApiResponse(responseCode = "403", description = "Caller does not own this client")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public ResponseEntity<PointsMovementResponse> adjust(
            @PathVariable UUID clientUuid,
            @Valid @RequestBody AdjustPointsRequest request,
            JwtAuthenticationToken token) {

        var entry = adjustPointsUseCase.adjust(
                extractExternalId(token), clientUuid, request.points(), request.notes());
        return ResponseEntity.ok(pointsRestMapper.toResponse(entry));
    }

    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("Expected JwtAuthenticationToken principal");
    }
}
