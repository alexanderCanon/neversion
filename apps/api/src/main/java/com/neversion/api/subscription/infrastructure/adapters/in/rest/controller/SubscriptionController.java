package com.neversion.api.subscription.infrastructure.adapters.in.rest.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase;
import com.neversion.api.subscription.application.port.in.ListSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.RevokeSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.RenewSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.UpdateSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.BatchCreateManualSubscriptionRequest;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.BatchCreateSubscriptionsResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.CreateManualSubscriptionRequest;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.DetectExpiredSubscriptionsResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.SubscriptionDetailResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.SubscriptionResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.mapper.SubscriptionMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Subscription lifecycle management (CU-A05, CU-A06)")
public class SubscriptionController {

    private final CreateManualSubscriptionUseCase createManualSubscriptionUseCase;
    private final BatchCreateSubscriptionsUseCase batchCreateSubscriptionsUseCase;
    private final UpdateSubscriptionUseCase updateSubscriptionUseCase;
    private final ListSubscriptionsUseCase listSubscriptionsUseCase;
    private final GetSubscriptionDetailUseCase getSubscriptionDetailUseCase;
    private final RenewSubscriptionUseCase renewSubscriptionUseCase;
    private final RevokeSubscriptionUseCase revokeSubscriptionUseCase;
    private final DetectExpiredSubscriptionsUseCase detectExpiredSubscriptionsUseCase;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionController(CreateManualSubscriptionUseCase createManualSubscriptionUseCase,
            BatchCreateSubscriptionsUseCase batchCreateSubscriptionsUseCase,
            UpdateSubscriptionUseCase updateSubscriptionUseCase,
            ListSubscriptionsUseCase listSubscriptionsUseCase,
            GetSubscriptionDetailUseCase getSubscriptionDetailUseCase,
            RenewSubscriptionUseCase renewSubscriptionUseCase,
            RevokeSubscriptionUseCase revokeSubscriptionUseCase,
            DetectExpiredSubscriptionsUseCase detectExpiredSubscriptionsUseCase,
            SubscriptionMapper subscriptionMapper) {
        this.createManualSubscriptionUseCase = createManualSubscriptionUseCase;
        this.batchCreateSubscriptionsUseCase = batchCreateSubscriptionsUseCase;
        this.updateSubscriptionUseCase = updateSubscriptionUseCase;
        this.listSubscriptionsUseCase = listSubscriptionsUseCase;
        this.getSubscriptionDetailUseCase = getSubscriptionDetailUseCase;
        this.renewSubscriptionUseCase = renewSubscriptionUseCase;
        this.revokeSubscriptionUseCase = revokeSubscriptionUseCase;
        this.detectExpiredSubscriptionsUseCase = detectExpiredSubscriptionsUseCase;
        this.subscriptionMapper = subscriptionMapper;
    }

    @PostMapping
    @Operation(summary = "Create manual subscription (US-048)",
            description = "Creates a subscription without a previous order or reservation. "
                    + "Anti-overbooking guard (BR-04) applied.")
    @ApiResponse(responseCode = "201", description = "Subscription created")
    @ApiResponse(responseCode = "400", description = "Validation or inventory state error")
    @ApiResponse(responseCode = "403", description = "Caller does not own selected resources")
    @ApiResponse(responseCode = "404", description = "Profile, Client or Service not found")
    @ApiResponse(responseCode = "409", description = "Profile already has an active subscription")
    public ResponseEntity<SubscriptionResponse> assign(
            @Valid @RequestBody CreateManualSubscriptionRequest request,
            JwtAuthenticationToken token) {
        Subscription subscription = subscriptionMapper.toDomain(request);
        Subscription created = createManualSubscriptionUseCase.create(
                subscription, request.sendNotification(), extractExternalId(token));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionMapper.toResponse(created));
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch create manual subscriptions",
            description = "Creates multiple subscriptions for a single client across multiple services. "
                    + "Supports auto-assignment of available profiles (profileId = null) or manual override. "
                    + "Partial success is possible: failed items are reported individually.")
    @ApiResponse(responseCode = "200", description = "Batch processed (check individual item results)")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Caller does not own selected resources")
    @ApiResponse(responseCode = "404", description = "Client, Service or Profile not found")
    public ResponseEntity<BatchCreateSubscriptionsResponse> batchCreate(
            @Valid @RequestBody BatchCreateManualSubscriptionRequest request,
            JwtAuthenticationToken token) {
        BatchCreateSubscriptionsUseCase.BatchCommand command = subscriptionMapper.toCommand(request);
        BatchCreateSubscriptionsUseCase.BatchResult result =
                batchCreateSubscriptionsUseCase.create(command, extractExternalId(token));
        return ResponseEntity.ok(subscriptionMapper.toBatchResponse(result));
    }

    @GetMapping
    @Operation(summary = "List vendor subscriptions (US-043)",
            description = "Returns subscriptions owned by the authenticated vendor.")
    @ApiResponse(responseCode = "200", description = "Subscription list")
    public ResponseEntity<List<SubscriptionResponse>> listSubscriptions(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) SubStatus status,
            JwtAuthenticationToken token) {

        List<SubscriptionListView> views = listSubscriptionsUseCase.listViews(
                serviceId, status, extractExternalId(token));

        return ResponseEntity.ok(views.stream().map(subscriptionMapper::toListResponse).toList());
    }

    @GetMapping("/vendor/{vendorUuid}")
    @Operation(summary = "List vendor subscriptions by vendor UUID (US-043, Legacy)",
            description = "Returns subscriptions owned by the authenticated vendor. "
                    + "Optional filters: serviceId (UUID) and status. "
                    + "Default sort: paymentDueDate ascending.")
    @ApiResponse(responseCode = "200", description = "Subscription list")
    @ApiResponse(responseCode = "403", description = "Caller does not own this vendor")
    @ApiResponse(responseCode = "404", description = "Vendor or service not found")
    public ResponseEntity<List<SubscriptionResponse>> listByVendor(
            @PathVariable UUID vendorUuid,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) SubStatus status,
            JwtAuthenticationToken token) {

        List<SubscriptionListView> views = listSubscriptionsUseCase.listViewsByVendor(
                vendorUuid, serviceId, status, extractExternalId(token));

        return ResponseEntity.ok(views.stream().map(subscriptionMapper::toListResponse).toList());
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get subscription detail (US-044)",
            description = "Returns subscription master data, commercial origin, client, profile, account, "
                    + "and financial snapshots. 403 if caller does not own this subscription.")
    @ApiResponse(responseCode = "200", description = "Subscription found")
    @ApiResponse(responseCode = "403", description = "Caller does not own this subscription")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    public ResponseEntity<SubscriptionDetailResponse> getById(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(subscriptionMapper.toDetailResponse(
                getSubscriptionDetailUseCase.getDetail(id, extractExternalId(token))));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend subscription (CU-A06)")
    @ApiResponse(responseCode = "200", description = "Subscription suspended")
    public ResponseEntity<SubscriptionResponse> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(
                subscriptionMapper.toResponse(updateSubscriptionUseCase.suspend(id)));
    }

    @PutMapping("/{id}/renew")
    @Operation(summary = "Renew subscription (US-045)",
            description = "Renews an ACTIVE or SUSPENDED subscription using BR-07.")
    @ApiResponse(responseCode = "200", description = "Subscription renewed")
    @ApiResponse(responseCode = "400", description = "Subscription cannot be renewed from its current status")
    @ApiResponse(responseCode = "403", description = "Caller does not own this subscription")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    public ResponseEntity<SubscriptionResponse> renew(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(subscriptionMapper.toResponse(
                renewSubscriptionUseCase.renew(id, extractExternalId(token))));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Revoke subscription access (US-046)",
            description = "Cancels the subscription and releases the assigned profile/account.")
    @ApiResponse(responseCode = "200", description = "Subscription cancelled and access revoked")
    @ApiResponse(responseCode = "400", description = "Subscription already cancelled")
    @ApiResponse(responseCode = "403", description = "Caller does not own this subscription")
    public ResponseEntity<SubscriptionResponse> cancel(
            @PathVariable UUID id,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(subscriptionMapper.toResponse(
                revokeSubscriptionUseCase.revoke(id, extractExternalId(token))));
    }

    @PostMapping("/detect-expired")
    @Operation(summary = "Detect expired subscriptions manually (US-047)",
            description = "SUPER_ADMIN manual trigger for the same process executed by the daily scheduler.")
    @ApiResponse(responseCode = "200", description = "Expired subscriptions processed")
    @ApiResponse(responseCode = "403", description = "Only SUPER_ADMIN can trigger this endpoint")
    public ResponseEntity<DetectExpiredSubscriptionsResponse> detectExpired() {
        int suspendedCount = detectExpiredSubscriptionsUseCase.detectAndSuspend();
        return ResponseEntity.ok(new DetectExpiredSubscriptionsResponse(suspendedCount));
    }

    /** Extracts the Supabase externalId (sub claim) from the JWT. */
    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
