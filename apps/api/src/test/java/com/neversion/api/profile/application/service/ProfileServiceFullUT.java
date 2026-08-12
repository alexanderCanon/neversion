package com.neversion.api.profile.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService unit tests — US-025/026/027")
class ProfileServiceFullUT {

    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    private ProfileService profileService;

    private static final UUID   ACCOUNT_UUID  = UUID.randomUUID();
    private static final UUID   PROFILE_UUID  = UUID.randomUUID();
    private static final String EXTERNAL_ID   = "supabase-user-ext";
    private static final Long   ACCOUNT_ID    = 10L;
    private static final Long   SERVICE_ID    = 99L;
    private static final Long   USER_ID       = 7L;
    private static final Long   VENDOR_ID     = 42L;
    private static final Long   OTHER_VENDOR  = 99L;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                userRepositoryPort,
                vendorRepositoryPort);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Account account(SaleMode mode) {
        return Account.builder()
                .id(ACCOUNT_ID).uuid(ACCOUNT_UUID)
                .vendorId(VENDOR_ID).serviceId(SERVICE_ID)
                .saleMode(mode).maxProfiles(5).build();
    }

    private Profile profile() {
        return Profile.builder()
                .id(1L).uuid(PROFILE_UUID)
                .accountId(ACCOUNT_ID).vendorId(VENDOR_ID)
                .name("Perfil 1").status(ProfileStatus.AVAILABLE)
                .build();
    }

    private void stubOwnership(Account account) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();
        Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).uuid(UUID.randomUUID()).build();
        when(accountRepositoryPort.findById(ACCOUNT_UUID)).thenReturn(Optional.of(account));
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private void stubProfileOwnership(Profile p) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();

        Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).uuid(UUID.randomUUID()).build();
        when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(p));
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
        when(accountRepositoryPort.findByInternalId(ACCOUNT_ID))
                .thenReturn(Optional.of(account(SaleMode.BY_PROFILE)));
    }

    // ── US-025: generate() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("US-025 generate()")
    class Generate {

        @Test
        @DisplayName("generate_fullAccountMode_shouldThrow409")
        void generate_fullAccountMode_shouldThrow409() {
            stubOwnership(account(SaleMode.FULL_ACCOUNT));

            assertThatThrownBy(() -> profileService.generate(ACCOUNT_UUID, 1, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot generate profiles");

            verify(profileRepositoryPort, never()).saveAll(any());
        }

        @Test
        @DisplayName("generate_exceedsMaxProfiles_shouldThrow409")
        void generate_exceedsMaxProfiles_shouldThrow409() {
            Account limited = Account.builder()
                    .id(ACCOUNT_ID).uuid(ACCOUNT_UUID)
                    .vendorId(VENDOR_ID).serviceId(SERVICE_ID)
                    .saleMode(SaleMode.BY_PROFILE).maxProfiles(2).build();
            stubOwnership(limited);
            when(profileRepositoryPort.findByAccountId(ACCOUNT_ID))
                    .thenReturn(List.of(profile(), profile()));

            assertThatThrownBy(() -> profileService.generate(ACCOUNT_UUID, 1, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("exceed maxProfiles");
        }

        @Test
        @DisplayName("generate_differentVendor_shouldThrow403")
        void generate_differentVendor_shouldThrow403() {
            Account account = account(SaleMode.BY_PROFILE);
            // Override account to belong to a different vendor
            Account foreignAccount = Account.builder()
                    .id(ACCOUNT_ID).uuid(ACCOUNT_UUID)
                    .vendorId(OTHER_VENDOR).serviceId(SERVICE_ID)
                    .saleMode(SaleMode.BY_PROFILE).build();

            User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();
            Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).uuid(UUID.randomUUID()).build();
            when(accountRepositoryPort.findById(ACCOUNT_UUID)).thenReturn(Optional.of(foreignAccount));
            when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
            when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));

            assertThatThrownBy(() -> profileService.generate(ACCOUNT_UUID, 1, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("generate_valid_shouldSaveAndReturnList")
        void generate_valid_shouldSaveAndReturnList() {
            stubOwnership(account(SaleMode.BY_PROFILE));
            when(profileRepositoryPort.findByAccountId(ACCOUNT_ID))
                    .thenReturn(List.of(profile()))
                    .thenReturn(List.of(profile(), profile(), profile()));

            List<Profile> result = profileService.generate(ACCOUNT_UUID, 2, EXTERNAL_ID);

            assertThat(result).hasSize(3);
            verify(profileRepositoryPort).saveAll(any());
        }
    }

    // ── US-026: update() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("US-026 update()")
    class Update {

        @Test
        @DisplayName("update_notFound_shouldThrow404")
        void update_notFound_shouldThrow404() {
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.update(PROFILE_UUID, "New", null, null, null, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(PROFILE_UUID.toString());
        }

        @Test
        @DisplayName("update_differentVendor_shouldThrow403")
        void update_differentVendor_shouldThrow403() {
            Profile foreignProfile = Profile.builder()
                    .id(1L).uuid(PROFILE_UUID)
                    .accountId(ACCOUNT_ID).vendorId(OTHER_VENDOR)
                    .build();
            Account foreignAccount = Account.builder()
                    .id(ACCOUNT_ID).vendorId(OTHER_VENDOR)
                    .saleMode(SaleMode.BY_PROFILE).build();

            User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();

            Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).uuid(UUID.randomUUID()).build();

            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(foreignProfile));
            when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
            when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
            when(accountRepositoryPort.findByInternalId(ACCOUNT_ID)).thenReturn(Optional.of(foreignAccount));

            assertThatThrownBy(() -> profileService.update(PROFILE_UUID, "New", null, null, null, EXTERNAL_ID))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("update_validName_shouldPatchOnlyName")
        void update_validName_shouldPatchOnlyName() {
            Profile p = profile();
            stubProfileOwnership(p);
            when(profileRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Profile result = profileService.update(PROFILE_UUID, "Nuevo Nombre", null, null, null, EXTERNAL_ID);

            assertThat(result.getName()).isEqualTo("Nuevo Nombre");
            assertThat(result.getPin()).isNull();
        }

        @Test
        @DisplayName("update_validPin_shouldPatchOnlyPin")
        void update_validPin_shouldPatchOnlyPin() {
            Profile p = profile();
            stubProfileOwnership(p);
            when(profileRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Profile result = profileService.update(PROFILE_UUID, null, "9876", null, null, EXTERNAL_ID);

            assertThat(result.getPin()).isEqualTo("9876");
            assertThat(result.getName()).isEqualTo("Perfil 1"); // unchanged
        }

        @Test
        @DisplayName("update_validNotes_shouldPatchOnlyNotes")
        void update_validNotes_shouldPatchOnlyNotes() {
            Profile p = profile();
            stubProfileOwnership(p);
            when(profileRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Profile result = profileService.update(PROFILE_UUID, null, null, "Link Spotify", null, EXTERNAL_ID);

            assertThat(result.getNotes()).isEqualTo("Link Spotify");
            assertThat(result.getName()).isEqualTo("Perfil 1"); // unchanged
        }
    }

    // ── US-027: changeStatus() ────────────────────────────────────────────────

    @Nested
    @DisplayName("US-027 changeStatus()")
    class ChangeStatus {

        @Test
        @DisplayName("changeStatus_systemControlledStatus_shouldThrow409")
        void changeStatus_activeStatus_shouldThrow409() {
            assertThatThrownBy(() ->
                    profileService.changeStatus(PROFILE_UUID, ProfileStatus.ACTIVE, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot be set manually");
        }

        @Test
        @DisplayName("changeStatus_reservedStatus_shouldThrow409")
        void changeStatus_reservedStatus_shouldThrow409() {
            assertThatThrownBy(() ->
                    profileService.changeStatus(PROFILE_UUID, ProfileStatus.RESERVED, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("cannot be set manually");
        }

        @Test
        @DisplayName("changeStatus_notFound_shouldThrow404")
        void changeStatus_notFound_shouldThrow404() {
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    profileService.changeStatus(PROFILE_UUID, ProfileStatus.BLOCKED, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("changeStatus_blocked_shouldUpdateStatus")
        void changeStatus_blocked_shouldUpdateStatus() {
            Profile p = profile();
            stubProfileOwnership(p);
            when(profileRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Profile result = profileService.changeStatus(PROFILE_UUID, ProfileStatus.BLOCKED, EXTERNAL_ID);

            assertThat(result.getStatus()).isEqualTo(ProfileStatus.BLOCKED);
        }

        @Test
        @DisplayName("changeStatus_available_shouldUpdateStatus")
        void changeStatus_available_shouldUpdateStatus() {
            Profile p = profile();
            p.setStatus(ProfileStatus.BLOCKED);
            stubProfileOwnership(p);
            when(profileRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Profile result = profileService.changeStatus(PROFILE_UUID, ProfileStatus.AVAILABLE, EXTERNAL_ID);

            assertThat(result.getStatus()).isEqualTo(ProfileStatus.AVAILABLE);
        }
    }

    // ── deleteById() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteById()")
    class Delete {

        @Test
        @DisplayName("delete_notFound_shouldThrow404")
        void delete_notFound_shouldThrow404() {
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.deleteById(PROFILE_UUID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(profileRepositoryPort, never()).deleteById(any());
        }

        @Test
        @DisplayName("delete_exists_shouldCallRepository")
        void delete_exists_shouldCallRepository() {
            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile()));

            profileService.deleteById(PROFILE_UUID);

            verify(profileRepositoryPort).deleteById(PROFILE_UUID);
        }
    }
}
