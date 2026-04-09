package com.neversion.api.profile.infrastructure.adapters.in.rest.controller;

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

import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileRequest;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileResponse;
import com.neversion.api.profile.infrastructure.adapters.in.rest.mapper.ProfileMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Profile (sub-profile) management within service accounts")
public class ProfileController {

    private final ProfileUseCase profileUseCase;
    private final ProfileMapper profileMapper;

    public ProfileController(ProfileUseCase profileUseCase, ProfileMapper profileMapper) {
        this.profileUseCase = profileUseCase;
        this.profileMapper = profileMapper;
    }

    @PostMapping
    @Operation(summary = "Create a profile manually",
            description = "Creates a profile under an account. Auto-generation happens on account creation.")
    @ApiResponse(responseCode = "201", description = "Profile created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ProfileResponse> create(@Valid @RequestBody ProfileRequest request) {
        Profile profile = profileMapper.toDomain(request);
        Profile created = profileUseCase.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileMapper.toResponse(created));
    }

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

    @GetMapping
    @Operation(summary = "List profiles by account",
            description = "Returns all profiles for a given account. Use ?available=true to filter only unassigned ones.")
    @ApiResponse(responseCode = "200", description = "Profile list")
    public ResponseEntity<List<ProfileResponse>> listByAccount(
            @RequestParam Long accountId,
            @RequestParam(required = false, defaultValue = "false") boolean available) {

        List<Profile> profiles = available
                ? profileUseCase.findAvailableByAccountId(accountId)
                : profileUseCase.findByAccountId(accountId);

        return ResponseEntity.ok(profiles.stream().map(profileMapper::toResponse).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a profile (name, pin, isOwner)")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<ProfileResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProfileRequest request) {
        return profileUseCase.findById(id).map(existing -> {
            if (request.name() != null) existing.setName(request.name());
            if (request.pin() != null) existing.setPin(request.pin());
            if (request.isOwner() != null) existing.setIsOwner(request.isOwner());
            Profile updated = profileUseCase.save(existing);
            return ResponseEntity.ok(profileMapper.toResponse(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a profile")
    @ApiResponse(responseCode = "204", description = "Profile deleted")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        profileUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
