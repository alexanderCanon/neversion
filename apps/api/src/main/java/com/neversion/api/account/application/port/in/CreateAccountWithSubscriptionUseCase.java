package com.neversion.api.account.application.port.in;

/**
 * Use case for creating a master account and immediately assigning a subscription
 * to an existing client in a single transaction.
 */
public interface CreateAccountWithSubscriptionUseCase {

    /**
     * Creates an account, auto-generates profiles, and assigns one profile
     * to the specified client as an active subscription.
     *
     * @param command          the account + subscription data
     * @param callerExternalId Supabase subject of the authenticated vendor
     * @return result containing the created account and subscription UUIDs
     */
    CreateAccountWithSubscriptionResult create(CreateAccountWithSubscriptionCommand command, String callerExternalId);
}
