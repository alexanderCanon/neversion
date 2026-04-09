package com.neversion.api.subscription.infrastructure.adapters.in.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.subscription.application.port.in.AssignSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.UpdateSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.CreateSubscriptionRequest;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.SubscriptionResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.mapper.SubscriptionMapper;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Subscription lifecycle management (CU-A05, CU-A06)")
public class SubscriptionController {

    private final AssignSubscriptionUseCase assignSubscriptionUseCase;
    private final UpdateSubscriptionUseCase updateSubscriptionUseCase;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionController(AssignSubscriptionUseCase assignSubscriptionUseCase,
            UpdateSubscriptionUseCase updateSubscriptionUseCase,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            SubscriptionMapper subscriptionMapper) {
        this.assignSubscriptionUseCase = assignSubscriptionUseCase;
        this.updateSubscriptionUseCase = updateSubscriptionUseCase;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.subscriptionMapper = subscriptionMapper;
    }

    @PostMapping
    @Operation(summary = "Assign subscription (CU-A05)",
            description = "Admin assigns a Client to a Profile. Anti-overbooking guard (BR-04) applied.")
    @ApiResponse(responseCode = "201", description = "Subscription created")
    @ApiResponse(responseCode = "404", description = "Profile or Client not found")
    @ApiResponse(responseCode = "409", description = "Profile already has an active subscription")
    public ResponseEntity<SubscriptionResponse> assign(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toDomain(request);
        Subscription created = assignSubscriptionUseCase.assign(subscription);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionMapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "List subscriptions (CU-A04)",
            description = "Filter by status, clientId (UUID), or profileId (UUID).")
    @ApiResponse(responseCode = "200", description = "Subscription list")
    public ResponseEntity<List<SubscriptionResponse>> list(
            @RequestParam(required = false) SubStatus status,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID profileId) {

        List<Subscription> subs;

        if (status != null) {
            subs = subscriptionRepositoryPort.findByStatus(status);
        } else if (clientId != null) {
            Long internalClientId = clientRepositoryPort.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId))
                    .getId();
            subs = subscriptionRepositoryPort.findByClientId(internalClientId);
        } else if (profileId != null) {
            Long internalProfileId = profileRepositoryPort.findById(profileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId))
                    .getId();
            subs = subscriptionRepositoryPort.findByProfileId(internalProfileId);
        } else {
            subs = subscriptionRepositoryPort.findAll();
        }

        return ResponseEntity.ok(subs.stream().map(subscriptionMapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by UUID")
    @ApiResponse(responseCode = "200", description = "Subscription found")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable UUID id) {
        return subscriptionRepositoryPort.findById(id)
                .map(subscriptionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend subscription (CU-A06)")
    @ApiResponse(responseCode = "200", description = "Subscription suspended")
    public ResponseEntity<SubscriptionResponse> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(
                subscriptionMapper.toResponse(updateSubscriptionUseCase.suspend(id)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel subscription (CU-A06)")
    @ApiResponse(responseCode = "200", description = "Subscription cancelled")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(
                subscriptionMapper.toResponse(updateSubscriptionUseCase.terminate(id)));
    }
}
