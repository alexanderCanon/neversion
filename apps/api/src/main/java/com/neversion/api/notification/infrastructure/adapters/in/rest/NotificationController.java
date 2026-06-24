package com.neversion.api.notification.infrastructure.adapters.in.rest;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.notification.application.port.in.SendManualReminderUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/v1/subscriptions")
@Tag(name = "Notifications", description = "Manual notification triggers")
public class NotificationController {

    private final SendManualReminderUseCase sendManualReminderUseCase;

    public NotificationController(SendManualReminderUseCase sendManualReminderUseCase) {
        this.sendManualReminderUseCase = sendManualReminderUseCase;
    }

    @PostMapping("/{subscriptionId}/remind")
    @Operation(summary = "Send manual renewal reminder",
            description = "Triggers a renewal reminder email for a specific subscription. "
                    + "The client must have an email address on file.")
    @ApiResponse(responseCode = "204", description = "Reminder recorded successfully")
    @ApiResponse(responseCode = "403", description = "Only vendors can trigger reminders")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    @ApiResponse(responseCode = "409", description = "Client has no email address")
    public ResponseEntity<Void> sendManualReminder(
            @PathVariable UUID subscriptionId,
            Principal principal) {
        sendManualReminderUseCase.sendReminder(subscriptionId, extractExternalId(principal));
        return ResponseEntity.noContent().build();
    }

    private String extractExternalId(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken().getSubject();
        }
        throw new IllegalStateException("No JWT principal found in security context");
    }
}
