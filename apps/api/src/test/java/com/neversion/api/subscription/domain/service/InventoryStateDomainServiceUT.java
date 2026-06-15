package com.neversion.api.subscription.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService.InventoryMutation;

@DisplayName("InventoryStateDomainService Unit Tests (tech-debt B1)")
class InventoryStateDomainServiceUT {

    private final InventoryStateDomainService service = new InventoryStateDomainService();

    private Profile profile(Long id, ProfileStatus status) {
        return Profile.builder().id(id).status(status).build();
    }

    private Account account(SaleMode saleMode, AccountStatus status) {
        return Account.builder().id(1L).saleMode(saleMode).status(status).build();
    }

    @Test
    @DisplayName("release BY_PROFILE - mutates only the selected profile and persists no account")
    void release_byProfile_shouldTouchOnlySelectedProfile() {
        Account account = account(SaleMode.BY_PROFILE, AccountStatus.PARTIAL);
        Profile selected = profile(10L, ProfileStatus.ACTIVE);
        Profile sibling = profile(11L, ProfileStatus.ACTIVE);

        InventoryMutation mutation = service.release(account, selected, List.of(selected, sibling));

        assertThat(selected.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
        assertThat(sibling.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(mutation.profilesToPersist()).containsExactly(selected);
        assertThat(mutation.account()).isEmpty();
    }

    @Test
    @DisplayName("release FULL_ACCOUNT - mutates all profiles and the account")
    void release_fullAccount_shouldTouchAllProfilesAndAccount() {
        Account account = account(SaleMode.FULL_ACCOUNT, AccountStatus.FULL);
        Profile selected = profile(10L, ProfileStatus.ACTIVE);
        Profile sibling = profile(11L, ProfileStatus.ACTIVE);
        List<Profile> all = List.of(selected, sibling);

        InventoryMutation mutation = service.release(account, selected, all);

        assertThat(selected.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
        assertThat(sibling.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.AVAILABLE);
        assertThat(mutation.profilesToPersist()).containsExactlyElementsOf(all);
        assertThat(mutation.account()).contains(account);
    }

    @Test
    @DisplayName("expire BY_PROFILE - mutates only the selected profile to EXPIRED")
    void expire_byProfile_shouldExpireOnlySelectedProfile() {
        Account account = account(SaleMode.BY_PROFILE, AccountStatus.PARTIAL);
        Profile selected = profile(10L, ProfileStatus.ACTIVE);
        Profile sibling = profile(11L, ProfileStatus.ACTIVE);

        InventoryMutation mutation = service.expire(account, selected, List.of(selected, sibling));

        assertThat(selected.getStatus()).isEqualTo(ProfileStatus.EXPIRED);
        assertThat(sibling.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(mutation.profilesToPersist()).containsExactly(selected);
        assertThat(mutation.account()).isEmpty();
    }

    @Test
    @DisplayName("expire FULL_ACCOUNT - mutates all profiles and the account to EXPIRED")
    void expire_fullAccount_shouldExpireAllProfilesAndAccount() {
        Account account = account(SaleMode.FULL_ACCOUNT, AccountStatus.FULL);
        Profile selected = profile(10L, ProfileStatus.ACTIVE);
        Profile sibling = profile(11L, ProfileStatus.ACTIVE);
        List<Profile> all = List.of(selected, sibling);

        InventoryMutation mutation = service.expire(account, selected, all);

        assertThat(selected.getStatus()).isEqualTo(ProfileStatus.EXPIRED);
        assertThat(sibling.getStatus()).isEqualTo(ProfileStatus.EXPIRED);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.EXPIRED);
        assertThat(mutation.profilesToPersist()).containsExactlyElementsOf(all);
        assertThat(mutation.account()).contains(account);
    }
}
