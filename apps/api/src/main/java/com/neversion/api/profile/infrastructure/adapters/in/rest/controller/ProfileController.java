package com.neversion.api.profile.infrastructure.adapters.in.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ChangeProfileStatusRequest;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileRequest;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileResponse;
import com.neversion.api.profile.infrastructure.adapters.in.rest.mapper.ProfileMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile management within service accounts (EPIC-03)")
public class ProfileController {

    private final ProfileUseCase profileUseCase;
    private final ProfileMapper profileMapper;

    public ProfileController(ProfileUseCase profileUseCase, ProfileMapper profileMapper) {
        this.profileUseCase = profileUseCase;
        this.profileMapper = profileMapper;
    }

    // ─── Create (manual) ─────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a profile manually",
            description = "Creates a profile under an account. Prefer POST /accounts/{id}/profiles/generate for bulk creation.")
    @ApiResponse(responseCode = "201", description = "Profile created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ProfileResponse> create(@Valid @RequestBody ProfileRequest request) {
        Profile profile = profileMapper.toDomain(request);
        Profile created = profileUseCase.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileMapper.toResponse(created));
    }

    // ─── Get by UUID ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get profile by UUID")
    @ApiResponse(responseCode = "200", description = "Profile found")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<ProfileResponse> getById(@PathVariable UUID id) {
        return profileUseCase.findById(id)
                .map(profileMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── List by account ──────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List profiles by account",
            description = "Returns all profiles for a given account. Use ?available=true for unassigned only.")
    @ApiResponse(responseCode = "200", description = "Profile list")
    public ResponseEntity<List<ProfileResponse>> listByAccount(
            @RequestParam UUID accountId,
            @RequestParam(required = false, defaultValue = "false") boolean available) {

        List<Profile> profiles = available
                ? profileUseCase.findAvailableByAccountUuid(accountId)
                : profileUseCase.findByAccountUuid(accountId);

        return ResponseEntity.ok(profiles.stream().map(profileMapper::toResponse).toList());
    }

    // ─── US-026: Update (name, pin, isOwner) ─────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update a profile (US-026)",
            description = "Updates name, pin, and/or isOwner. Status is not editable here. Requires ownership.")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "403", description = "Caller does not own the account for this profile")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<ProfileResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProfileRequest request,
            JwtAuthenticationToken token) {
        Profile updated = profileUseCase.update(
                id, request.name(), request.pin(), request.notes(), request.isOwner(),
                token.getToken().getSubject());
        return ResponseEntity.ok(profileMapper.toResponse(updated));
    }

    // ─── US-027: Change status ────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change profile status (US-027)",
            description = "Manually sets status to AVAILABLE or BLOCKED. ACTIVE/RESERVED/OCCUPIED/EXPIRED are system-controlled.")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Invalid or system-controlled status")
    @ApiResponse(responseCode = "403", description = "Caller does not own the account for this profile")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<ProfileResponse> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeProfileStatusRequest request,
            JwtAuthenticationToken token) {
        Profile updated = profileUseCase.changeStatus(
                id, request.status(), token.getToken().getSubject());
        return ResponseEntity.ok(profileMapper.toResponse(updated));
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a profile")
    @ApiResponse(responseCode = "204", description = "Profile deleted")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        profileUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
