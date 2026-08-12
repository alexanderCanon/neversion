package com.neversion.api.profile.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService unit tests")
class ProfileServiceUT {

    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    private ProfileService profileService;

    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final String EXTERNAL_ID = "supabase-user-id";
    private static final Long ACCOUNT_ID = 10L;
    private static final Long SERVICE_ID = 99L;
    private static final Long USER_ID = 7L;
    private static final Long VENDOR_ID = 42L;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                userRepositoryPort,
                vendorRepositoryPort);
    }

    @Test
    @DisplayName("should reject manual profile generation for full-account sale mode")
    void generate_shouldRejectFullAccountMode() {
        Account account = account(SaleMode.FULL_ACCOUNT);

        stubOwnership(account);

        assertThatThrownBy(() -> profileService.generate(ACCOUNT_UUID, 1, EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot generate profiles");

        verify(profileRepositoryPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("should generate profiles for by-profile sale mode within service limit")
    void generate_shouldCreateProfilesForByProfileMode() {
        Account account = account(SaleMode.BY_PROFILE);
        Profile existingProfile = Profile.builder().id(1L).accountId(ACCOUNT_ID).vendorId(VENDOR_ID).build();
        List<Profile> updatedProfiles = List.of(
                existingProfile,
                Profile.builder().id(2L).accountId(ACCOUNT_ID).vendorId(VENDOR_ID).build(),
                Profile.builder().id(3L).accountId(ACCOUNT_ID).vendorId(VENDOR_ID).build());

        stubOwnership(account);
        when(profileRepositoryPort.findByAccountId(ACCOUNT_ID))
                .thenReturn(List.of(existingProfile))
                .thenReturn(updatedProfiles);

        List<Profile> result = profileService.generate(ACCOUNT_UUID, 2, EXTERNAL_ID);

        assertThat(result).hasSize(3);
        verify(profileRepositoryPort).saveAll(argThat(profiles ->
                profiles.size() == 2
                        && profiles.stream().allMatch(profile -> ACCOUNT_ID.equals(profile.getAccountId()))
                        && profiles.stream().noneMatch(profile -> Boolean.TRUE.equals(profile.getIsOwner()))));
    }

    private void stubOwnership(Account account) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();

        Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).uuid(UUID.randomUUID()).build();

        when(accountRepositoryPort.findById(ACCOUNT_UUID)).thenReturn(Optional.of(account));
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Account account(SaleMode saleMode) {
        return Account.builder()
                .id(ACCOUNT_ID)
                .uuid(ACCOUNT_UUID)
                .vendorId(VENDOR_ID)
                .serviceId(SERVICE_ID)
                .saleMode(saleMode)
                .maxProfiles(5)
                .build();
    }
}
