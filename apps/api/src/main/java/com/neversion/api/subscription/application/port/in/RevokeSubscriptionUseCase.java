package com.neversion.api.subscription.application.port.in;

import java.util.UUID;

import com.neversion.api.subscription.domain.model.Subscription;

public interface RevokeSubscriptionUseCase {

    /**
     * US-046: Revokes access and releases the assigned inventory.
     */
    Subscription revoke(UUID subscriptionUuid, String callerExternalId);
}
