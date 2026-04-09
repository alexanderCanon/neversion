package com.neversion.api.account.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.application.port.in.CreateAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;


/**
 * CU-A01: Admin creates a master Account for a Service.
 * Upon creation, N blank Profiles are auto-generated based on service.maxProfiles (BR-01).
 * Only BY_PROFILE accounts auto-generate profiles; FULL_ACCOUNT skips this.
 */
@Service
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ProfileUseCase profileUseCase;

    public CreateAccountService(AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ProfileUseCase profileUseCase) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.profileUseCase = profileUseCase;
    }

    @Override
    @Transactional
    public Account create(Account account) {
        if (account.getRenewalDate() == null) {
            throw new BusinessRuleException("Renewal date is required");
        }

        com.neversion.api.service.domain.model.Service service = serviceRepositoryPort.findByInternalId(account.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service not found: " + account.getServiceId()));

        Account saved = accountRepositoryPort.save(account);

        // Auto-generate profiles only for by_profile sale mode (BR-01)
        if (account.getSaleMode() != null) {
            switch (account.getSaleMode()) {
                case BY_PROFILE -> {
                    int count = service.getMaxProfiles() != null ? service.getMaxProfiles() : 1;
                    profileUseCase.generateProfilesForAccount(saved.getId(), count);
                }
                case FULL_ACCOUNT -> { /* No granular profiles for full-account sales */ }
            }
        }

        return saved;
    }
}
