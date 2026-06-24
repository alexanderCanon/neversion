package com.neversion.api.notification.application.port.in;

import java.util.UUID;

/**
 * Use case for manually triggering a renewal reminder notification
 * for a specific subscription.
 */
public interface SendManualReminderUseCase {

    /**
     * Records a manual renewal reminder notification for the given subscription,
     * provided the client has an email address on file.
     *
     * @param subscriptionId   UUID of the subscription to remind
     * @param callerExternalId Supabase subject of the authenticated vendor
     * @throws com.neversion.api.exception.ResourceNotFoundException if the subscription does not exist
     * @throws com.neversion.api.exception.BusinessRuleException      if the client has no email
     * @throws org.springframework.security.access.AccessDeniedException if the vendor does not own the subscription
     */
    void sendReminder(UUID subscriptionId, String callerExternalId);
}
