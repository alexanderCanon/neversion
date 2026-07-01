package com.neversion.api.loyalty.infrastructure.adapters.in.rest.controller;

import java.security.Principal;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.loyalty.application.port.in.GetPointsSummaryUseCase;
import com.neversion.api.loyalty.application.port.in.ListPointsMovementsUseCase;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsMovementsPageResponse;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto.PointsSummaryResponse;
import com.neversion.api.loyalty.infrastructure.adapters.in.rest.mapper.PointsRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Store-facing endpoints for the authenticated client to view their loyalty points balance
 * and movement history. The client is always resolved from the JWT — no client id parameter.
 */
@RestController
@RequestMapping(value = "/api/v1/clients/me/points", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Loyalty Points", description = "Loyalty points management and history")
public class ClientPointsController {

    private final GetPointsSummaryUseCase getPointsSummaryUseCase;
    private final ListPointsMovementsUseCase listPointsMovementsUseCase;
    private final PointsRestMapper pointsRestMapper;

    public ClientPointsController(
            GetPointsSummaryUseCase getPointsSummaryUseCase,
            ListPointsMovementsUseCase listPointsMovementsUseCase,
            PointsRestMapper pointsRestMapper) {
        this.getPointsSummaryUseCase = getPointsSummaryUseCase;
        this.listPointsMovementsUseCase = listPointsMovementsUseCase;
        this.pointsRestMapper = pointsRestMapper;
    }

    @GetMapping
    @Operation(summary = "Get my loyalty points balance",
            description = "Returns the authenticated client's points summary (available/pending/total).")
    @ApiResponse(responseCode = "200", description = "Points summary")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<PointsSummaryResponse> getMySummary(JwtAuthenticationToken token) {
        return ResponseEntity.ok(pointsRestMapper.toResponse(
                getPointsSummaryUseCase.getForAuthenticatedClient(extractExternalId(token))));
    }

    @GetMapping("/movements")
    @Operation(summary = "Get my loyalty points movement history",
            description = "Returns a paginated list of the authenticated client's points ledger movements.")
    @ApiResponse(responseCode = "200", description = "Points movements")
    @ApiResponse(responseCode = "404", description = "Client record not found for the user")
    public ResponseEntity<PointsMovementsPageResponse> getMyMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            JwtAuthenticationToken token) {

        Pageable pageable = PageRequest.of(page, size);
        var result = listPointsMovementsUseCase.listForAuthenticatedClient(extractExternalId(token), pageable);
        return ResponseEntity.ok(new PointsMovementsPageResponse(
                pointsRestMapper.toResponseList(result), result.totalElements()));
    }

    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("Expected JwtAuthenticationToken principal");
    }
}
