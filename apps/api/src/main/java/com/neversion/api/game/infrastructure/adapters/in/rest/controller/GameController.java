package com.neversion.api.game.infrastructure.adapters.in.rest.controller;

import com.neversion.api.game.application.port.in.GameUseCase;
import com.neversion.api.game.infrastructure.adapters.in.rest.dto.GameRequest;
import com.neversion.api.game.infrastructure.adapters.in.rest.dto.GameResponse;
import com.neversion.api.game.infrastructure.adapters.in.rest.mapper.GameMapper;
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
@RequestMapping(value = "/api/v1/games", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Games", description = "Game parent catalog management (groups GameSkus)")
public class GameController {

    private final GameUseCase gameUseCase;
    private final GameMapper gameMapper;

    public GameController(GameUseCase gameUseCase, GameMapper gameMapper) {
        this.gameUseCase = gameUseCase;
        this.gameMapper = gameMapper;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a game (parent)", description = "Creates a new game parent (e.g. Free Fire) in the caller vendor's catalog.")
    @ApiResponse(responseCode = "201", description = "Game created")
    @ApiResponse(responseCode = "400", description = "Validation or duplicate slug error")
    public ResponseEntity<GameResponse> create(
            @Valid @RequestBody GameRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var domain = gameMapper.toDomain(request);
        var created = gameUseCase.create(domain, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a game (parent)", description = "Updates editable fields of a game. Caller must own the game.")
    @ApiResponse(responseCode = "200", description = "Game updated")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<GameResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody GameRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var domain = gameMapper.toDomain(request);
        var updated = gameUseCase.update(id, domain, jwt.getSubject());
        return ResponseEntity.ok(gameMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle game active status", description = "Toggles active status. Caller must own the game.")
    @ApiResponse(responseCode = "200", description = "Status toggled")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<GameResponse> toggleStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        var toggled = gameUseCase.toggleStatus(id, jwt.getSubject());
        return ResponseEntity.ok(gameMapper.toResponse(toggled));
    }

    @GetMapping("/vendor")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List vendor games - panel view", description = "Returns all games for the authenticated vendor.")
    @ApiResponse(responseCode = "200", description = "Game list")
    public ResponseEntity<List<GameResponse>> listVendorGames(
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal Jwt jwt) {
        var list = gameUseCase.listByVendor(isActive, jwt.getSubject());
        return ResponseEntity.ok(list.stream().map(gameMapper::toResponse).toList());
    }

    @GetMapping("/vendor/{vendorUuid}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List vendor games by vendor UUID - panel view (Legacy)", description = "Returns all games for the given vendor. Owner only.")
    @ApiResponse(responseCode = "200", description = "Game list")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<List<GameResponse>> listByVendor(
            @PathVariable UUID vendorUuid,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal Jwt jwt) {
        var list = gameUseCase.listByVendor(vendorUuid, isActive, jwt.getSubject());
        return ResponseEntity.ok(list.stream().map(gameMapper::toResponse).toList());
    }


    @GetMapping("/store/{vendorUuid}")
    @Operation(summary = "List active games - store view", description = "Returns active game parents for the given vendor. No auth.")
    @ApiResponse(responseCode = "200", description = "Active game list")
    public ResponseEntity<List<GameResponse>> listActive(@PathVariable UUID vendorUuid) {
        var list = gameUseCase.listActive(vendorUuid);
        return ResponseEntity.ok(list.stream().map(gameMapper::toResponse).toList());
    }

    @GetMapping("/store/{vendorUuid}/by-slug/{slug}")
    @Operation(summary = "Get active game by slug - store view", description = "Returns an active game parent by its slug. No auth.")
    @ApiResponse(responseCode = "200", description = "Game found")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<GameResponse> getActiveBySlug(
            @PathVariable UUID vendorUuid,
            @PathVariable String slug) {
        var game = gameUseCase.getActiveBySlug(vendorUuid, slug);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get game by UUID")
    @ApiResponse(responseCode = "200", description = "Game found")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<GameResponse> getById(@PathVariable UUID id) {
        var game = gameUseCase.getById(id);
        return ResponseEntity.ok(gameMapper.toResponse(game));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all games (SUPER_ADMIN)")
    @ApiResponse(responseCode = "200", description = "All games list")
    public ResponseEntity<List<GameResponse>> list() {
        var list = gameUseCase.getAll();
        return ResponseEntity.ok(list.stream().map(gameMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft-deactivate a game", description = "Logically deactivates a game. Caller must own the game.")
    @ApiResponse(responseCode = "204", description = "Game logically deleted")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Game not found")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        gameUseCase.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
