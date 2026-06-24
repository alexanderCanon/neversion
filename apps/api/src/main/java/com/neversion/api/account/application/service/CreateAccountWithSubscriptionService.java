package com.neversion.api.account.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.application.port.in.CreateAccountWithSubscriptionCommand;
import com.neversion.api.account.application.port.in.CreateAccountWithSubscriptionResult;
import com.neversion.api.account.application.port.in.CreateAccountWithSubscriptionUseCase;
import com.neversion.api.account.application.port.in.CreateAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;

/**
 * Unified use case: creates a master account and immediately assigns
 * a subscription to an existing client in a single transaction.
 *
 * Delegates to {@link CreateAccountUseCase} for account creation and
 * profile auto-generation, then selects an available profile and
 * delegates to {@link CreateManualSubscriptionUseCase} for the subscription.
 */
@Service
public class CreateAccountWithSubscriptionService implements CreateAccountWithSubscriptionUseCase {

    private final CreateAccountUseCase createAccountUseCase;
    private final CreateManualSubscriptionUseCase createManualSubscriptionUseCase;
    private final ProfileRepositoryPort profileRepositoryPort;

    public CreateAccountWithSubscriptionService(
            CreateAccountUseCase createAccountUseCase,
            CreateManualSubscriptionUseCase createManualSubscriptionUseCase,
            ProfileRepositoryPort profileRepositoryPort) {
        this.createAccountUseCase = createAccountUseCase;
        this.createManualSubscriptionUseCase = createManualSubscriptionUseCase;
        this.profileRepositoryPort = profileRepositoryPort;
    }

    @Override
    @Transactional
    public CreateAccountWithSubscriptionResult create(CreateAccountWithSubscriptionCommand cmd, String callerExternalId) {
        Account accountToCreate = Account.builder()
                .email(cmd.email())
                .password(cmd.password())
                .serviceUuid(cmd.serviceUuid())
                .saleMode(SaleMode.valueOf(cmd.saleMode()))
                .renewalDate(cmd.renewalDate())
                .plan(cmd.plan())
                .cost(cmd.cost())
                .source(cmd.source())
                .purchasedAt(cmd.purchasedAt())
                .notes(cmd.accountNotes())
                .maxProfiles(cmd.maxProfiles())
                .build();

        Account savedAccount = createAccountUseCase.create(accountToCreate, callerExternalId);

        Profile selectedProfile = selectProfile(savedAccount);

        Subscription subscriptionToCreate = Subscription.builder()
                .clientUuid(cmd.clientUuid())
                .profileUuid(selectedProfile.getUuid())
                .serviceUuid(cmd.serviceUuid())
                .paymentDueDate(cmd.paymentDueDate())
                .priceSold(cmd.priceSold())
                .discountApplied(cmd.discountApplied())
                .notes(cmd.subscriptionNotes())
                .build();

        Subscription savedSubscription = createManualSubscriptionUseCase.create(
                subscriptionToCreate, cmd.sendNotification(), callerExternalId);

        return new CreateAccountWithSubscriptionResult(
                savedAccount.getUuid(),
                savedSubscription.getUuid());
    }

    private Profile selectProfile(Account account) {
        List<Profile> profiles = profileRepositoryPort.findByAccountId(account.getId());
        if (profiles.isEmpty()) {
            throw new BusinessRuleException("No profiles were generated for the account.");
        }

        if (account.getSaleMode() == SaleMode.FULL_ACCOUNT) {
            return profiles.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsOwner()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "No owner profile found for FULL_ACCOUNT."));
        }

        return profiles.stream()
                .filter(p -> p.getStatus() == ProfileStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "No available profile found for BY_PROFILE account."));
    }
}
