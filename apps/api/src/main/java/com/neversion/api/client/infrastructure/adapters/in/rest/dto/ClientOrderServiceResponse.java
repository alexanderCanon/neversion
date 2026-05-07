package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Service line shown in the authenticated client's order history.")
public record ClientOrderServiceResponse(
        UUID serviceId,
        String serviceName,
        Integer quantity) {
}
