package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ClientAccessResponse(
        UUID subscriptionId,
        String serviceName,
        String accountEmail,
        String accountPassword,
        String profileName,
        String profilePin,
        LocalDate paymentDueDate,
        String status) {
}
