package com.neversion.api.subscription.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

public interface ListSubscriptionsUseCase {

    /**
     * US-043: Returns subscriptions owned by the authenticated vendor.
     *
     * @param vendorUuid       public vendor identifier from the route
     * @param serviceUuid      optional public service identifier filter
     * @param status           optional subscription status filter
     * @param callerExternalId Supabase subject from the JWT
     */
    List<Subscription> listByVendor(UUID vendorUuid, UUID serviceUuid, SubStatus status,
            String callerExternalId);
}
