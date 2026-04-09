package com.neversion.api.subscription.application.port.in;

import java.util.UUID;

import com.neversion.api.subscription.domain.model.Subscription;

/**
 * CU-A06: Subscription lifecycle management.
 */
public interface UpdateSubscriptionUseCase {

    Subscription suspend(UUID id);

    Subscription terminate(UUID id);
}
