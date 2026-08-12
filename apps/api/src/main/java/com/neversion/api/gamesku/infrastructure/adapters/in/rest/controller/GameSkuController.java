package com.neversion.api.gamesku.infrastructure.adapters.in.rest.controller;

import com.neversion.api.gamesku.application.port.in.GameSkuUseCase;
import com.neversion.api.gamesku.domain.model.GameSku;
import com.neversion.api.gamesku.infrastructure.adapters.in.rest.dto.GameSkuRequest;
import com.neversion.api.gamesku.infrastructure.adapters.in.rest.dto.GameSkuResponse;
import com.neversion.api.gamesku.infrastructure.adapters.in.rest.mapper.GameSkuMapper;
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

@RestController
@RequestMapping(value = "/api/v1/game-skus", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Game SKUs", description = "Game SKU (recharge) management — children of a Game parent")
public class GameSkuController {

    private final GameSkuUseCase gameSkuUseCase;
    private final GameSkuMapper gameSkuMapper;

    public GameSkuController(GameSkuUseCase gameSkuUseCase, GameSkuMapper gameSkuMapper) {
        this.gameSkuUseCase = gameSkuUseCase;
        this.gameSkuMapper = gameSkuMapper;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a game SKU", description = "Creates a new SKU (e.g. Free Fire 110 Diamonds) in the caller vendor's catalog.")
    @ApiResponse(responseCode = "201", description = "Game SKU created")
    @ApiResponse(responseCode = "400", description = "Validation or duplicate code error")
    public ResponseEntity<GameSkuResponse> create(
            @Valid @RequestBody GameSkuRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var domain = gameSkuMapper.toDomain(request);
        var created = gameSkuUseCase.create(domain, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(gameSkuMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a game SKU", description = "Updates editable fields of a SKU. Caller must own the SKU.")
    @ApiResponse(responseCode = "200", description = "Game SKU updated")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Game SKU not found")
    public ResponseEntity<GameSkuResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody GameSkuRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var domain = gameSkuMapper.toDomain(request);
        var updated = gameSkuUseCase.update(id, domain, jwt.getSubject());
        return ResponseEntity.ok(gameSkuMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle game SKU active status", description = "Toggles active status. Caller must own the SKU.")
    @ApiResponse(responseCode = "200", description = "Status toggled")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Game SKU not found")
    public ResponseEntity<GameSkuResponse> toggleStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        var toggled = gameSkuUseCase.toggleStatus(id, jwt.getSubject());
        return ResponseEntity.ok(gameSkuMapper.toResponse(toggled));
    }

    @GetMapping("/vendor")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List vendor game SKUs - panel view", description = "Returns all SKUs for the authenticated vendor, optionally filtered by game.")
    @ApiResponse(responseCode = "200", description = "Game SKU list")
    public ResponseEntity<List<GameSkuResponse>> listVendorGameSkus(
            @RequestParam(required = false) UUID gameUuid,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal Jwt jwt) {
        var list = gameSkuUseCase.listByVendor(gameUuid, isActive, jwt.getSubject());
        return ResponseEntity.ok(list.stream().map(gameSkuMapper::toResponse).toList());
    }

    @GetMapping("/vendor/{vendorUuid}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List vendor game SKUs by vendor UUID - panel view (Legacy)", description = "Returns all SKUs for the given vendor, optionally filtered by game. Owner only.")
    @ApiResponse(responseCode = "200", description = "Game SKU list")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<List<GameSkuResponse>> listByVendor(
            @PathVariable UUID vendorUuid,
            @RequestParam(required = false) UUID gameUuid,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal Jwt jwt) {
        var list = gameSkuUseCase.listByVendor(vendorUuid, gameUuid, isActive, jwt.getSubject());
        return ResponseEntity.ok(list.stream().map(gameSkuMapper::toResponse).toList());
    }


    @GetMapping("/store/{vendorUuid}")
    @Operation(summary = "List active game SKUs by game slug - store view", description = "Returns active SKUs for a game identified by slug. No auth.")
    @ApiResponse(responseCode = "200", description = "Active game SKU list")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<List<GameSkuResponse>> listActiveByGameSlug(
            @PathVariable UUID vendorUuid,
            @RequestParam String gameSlug) {
        var list = gameSkuUseCase.listActiveByGameSlug(vendorUuid, gameSlug);
        return ResponseEntity.ok(list.stream().map(gameSkuMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get game SKU by UUID")
    @ApiResponse(responseCode = "200", description = "Game SKU found")
    @ApiResponse(responseCode = "404", description = "Game SKU not found")
    public ResponseEntity<GameSkuResponse> getById(@PathVariable UUID id) {
        var gameSku = gameSkuUseCase.getById(id);
        return ResponseEntity.ok(gameSkuMapper.toResponse(gameSku));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all game SKUs (SUPER_ADMIN)")
    @ApiResponse(responseCode = "200", description = "All game SKUs list")
    public ResponseEntity<List<GameSkuResponse>> list() {
        var list = gameSkuUseCase.getAll();
        return ResponseEntity.ok(list.stream().map(gameSkuMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft-deactivate a game SKU", description = "Logically deactivates a SKU. Caller must own the SKU.")
    @ApiResponse(responseCode = "204", description = "Game SKU logically deleted")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Game SKU not found")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        gameSkuUseCase.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
