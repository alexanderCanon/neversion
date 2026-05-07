package com.neversion.api.notification.infrastructure.adapters.in.rest.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neversion.api.notification.application.port.in.ProcessNotificationsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * EPIC-08: Notification management endpoints.
 * POST /process: manual trigger for testing/n8n.
 */
@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Notification processing and management (EPIC-08)")
public class NotificationProcessController {

    private final ProcessNotificationsUseCase processNotificationsUseCase;

    public NotificationProcessController(ProcessNotificationsUseCase processNotificationsUseCase) {
        this.processNotificationsUseCase = processNotificationsUseCase;
    }

    @PostMapping("/process")
    @Operation(summary = "Process pending notifications",
            description = "Manual trigger to process pending notifications. Restricted to SUPER_ADMIN.")
    @ApiResponse(responseCode = "200", description = "Processing result")
    public ResponseEntity<Map<String, Object>> processNotifications(
            @RequestParam(defaultValue = "50") int batchSize) {

        int processed = processNotificationsUseCase.processNextBatch(batchSize);
        return ResponseEntity.ok(Map.of(
                "processed", processed,
                "batchSize", batchSize));
    }

    @GetMapping("/health")
    @Operation(summary = "Notification system health check")
    @ApiResponse(responseCode = "200", description = "System status")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "module", "notifications"));
    }
}
