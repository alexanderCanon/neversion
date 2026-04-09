package com.neversion.api.dashboard.application.result;

import java.util.UUID;

/**
 * Customer data nested inside a subscription result.
 */
public record ProfileCustomerResult(
        UUID id,
        String name,
        String phone,
        String type) {
}
