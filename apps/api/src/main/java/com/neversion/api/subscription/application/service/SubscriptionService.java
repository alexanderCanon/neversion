package com.neversion.api.subscription.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.exception.AccountOverbookingException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.subscription.application.port.in.AssignSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;

/**
 * CU-A05: Admin manually assigns a Client to a Profile, creating an active Subscription.
 *
 * UUID-to-Long resolution:
 *   The REST layer passes UUID fields (profileUuid, clientUuid).
 *   This service resolves them to internal Long IDs before persistence.
 *
 * Anti-overbooking (BR-04):
 *   A profile can only hold ONE active subscription at a time.
 */
@Service
public class SubscriptionService implements AssignSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public SubscriptionService(SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            ClientRepositoryPort clientRepositoryPort) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    @Transactional
    public Subscription assign(Subscription subscription) {
        // 1. Resolve Profile UUID → internal Long ID
        Profile profile = profileRepositoryPort.findById(subscription.getProfileUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found: " + subscription.getProfileUuid()));

        // 2. Resolve Client UUID → internal Long ID
        Client client = clientRepositoryPort.findById(subscription.getClientUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found: " + subscription.getClientUuid()));

        // 3. Anti-overbooking: profile cannot have two active subscriptions (BR-04)
        if (subscriptionRepositoryPort.existsActiveByProfileId(profile.getId())) {
            throw new AccountOverbookingException(
                    "Profile " + subscription.getProfileUuid()
                            + " already has an active subscription. "
                            + "Cancel or suspend the existing one before reassigning.");
        }

        // 4. Set resolved IDs, default status and persist
        subscription.setProfileId(profile.getId());
        subscription.setClientId(client.getId());
        subscription.setStatus(SubStatus.ACTIVE);

        return subscriptionRepositoryPort.save(subscription);
    }
}
