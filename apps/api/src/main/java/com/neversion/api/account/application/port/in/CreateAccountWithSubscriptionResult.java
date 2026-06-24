package com.neversion.api.account.application.port.in;

import java.util.UUID;

/**
 * Result of creating an account with an immediate subscription assignment.
 */
public record CreateAccountWithSubscriptionResult(
        UUID accountUuid,
        UUID subscriptionUuid) {
}
