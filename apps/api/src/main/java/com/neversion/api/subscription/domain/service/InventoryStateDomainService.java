package com.neversion.api.subscription.domain.service;

import java.util.List;
import java.util.Optional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

/**
 * Tech-debt remediation B1: single source of truth for inventory state
 * transitions when a subscription ends.
 * <p>
 * Pure domain service (no ports, no persistence). It only mutates the entities
 * it receives and returns the set the caller must persist. The application
 * layer is responsible for loading the account's profiles and saving the
 * results, keeping the {@code FULL_ACCOUNT} vs per-profile rule encapsulated
 * here instead of being duplicated across services.
 */
public class InventoryStateDomainService {

    /**
     * Result of an inventory state transition: the entities the caller must
     * persist. {@code accountToPersist} is only present for {@code FULL_ACCOUNT}
     * sales, where the master account status also changes.
     */
    public record InventoryMutation(List<Profile> profilesToPersist, Account accountToPersist) {
        public Optional<Account> account() {
            return Optional.ofNullable(accountToPersist);
        }
    }

    /** US-046: releases inventory back to AVAILABLE when access is revoked. */
    public InventoryMutation release(Account account, Profile selectedProfile, List<Profile> accountProfiles) {
        return apply(account, selectedProfile, accountProfiles,
                ProfileStatus.AVAILABLE, AccountStatus.AVAILABLE);
    }

    /** US-047: marks inventory as EXPIRED when a subscription lapses. */
    public InventoryMutation expire(Account account, Profile selectedProfile, List<Profile> accountProfiles) {
        return apply(account, selectedProfile, accountProfiles,
                ProfileStatus.EXPIRED, AccountStatus.EXPIRED);
    }

    private InventoryMutation apply(Account account, Profile selectedProfile, List<Profile> accountProfiles,
            ProfileStatus profileStatus, AccountStatus accountStatus) {
        if (account.getSaleMode() == SaleMode.FULL_ACCOUNT) {
            accountProfiles.forEach(profile -> profile.setStatus(profileStatus));
            account.setStatus(accountStatus);
            return new InventoryMutation(accountProfiles, account);
        }

        selectedProfile.setStatus(profileStatus);
        return new InventoryMutation(List.of(selectedProfile), null);
    }
}
