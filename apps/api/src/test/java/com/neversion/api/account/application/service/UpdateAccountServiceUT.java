package com.neversion.api.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateAccountService unit tests")
class UpdateAccountServiceUT {

    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;

    private UpdateAccountService updateAccountService;

    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final String EXTERNAL_ID = "supabase-user-id";
    private static final Long USER_ID = 7L;
    private static final Long VENDOR_ID = 42L;

    @BeforeEach
    void setUp() {
        updateAccountService = new UpdateAccountService(
                accountRepositoryPort, serviceRepositoryPort, profileRepositoryPort,
                subscriptionRepositoryPort, userRepositoryPort, vendorRepositoryPort);
    }

    @Test
    @DisplayName("should update editable fields when sale mode is unchanged")
    void update_shouldPersistEditableFields_whenSaleModeIsUnchanged() {
        Account existing = existingAccount(SaleMode.BY_PROFILE);
        Account updates = updatePayload(SaleMode.BY_PROFILE);

        stubOwnership(existing);
        when(accountRepositoryPort.save(existing)).thenReturn(existing);

        Account result = updateAccountService.update(ACCOUNT_UUID, updates, EXTERNAL_ID);

        assertThat(result.getSaleMode()).isEqualTo(SaleMode.BY_PROFILE);
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPassword()).isEqualTo("updated-password");
        assertThat(result.getPlan()).isEqualTo("Premium");
        verify(accountRepositoryPort).save(existing);
    }

    @Test
    @DisplayName("should allow sale mode change when no active subscriptions")
    void update_shouldAllowSaleModeChange_whenNoActiveSubscriptions() {
        Account existing = existingAccount(SaleMode.BY_PROFILE);
        Account updates = updatePayload(SaleMode.FULL_ACCOUNT);

        stubOwnership(existing);
        when(profileRepositoryPort.findByAccountId(existing.getId())).thenReturn(java.util.List.of());
        when(accountRepositoryPort.save(existing)).thenReturn(existing);

        Account result = updateAccountService.update(ACCOUNT_UUID, updates, EXTERNAL_ID);

        assertThat(result.getSaleMode()).isEqualTo(SaleMode.FULL_ACCOUNT);
        verify(accountRepositoryPort).save(existing);
    }

    @Test
    @DisplayName("should reject sale mode change when account has active subscriptions")
    void update_shouldRejectSaleModeChange_whenActiveSubscriptions() {
        Account existing = existingAccount(SaleMode.BY_PROFILE);
        Account updates = updatePayload(SaleMode.FULL_ACCOUNT);

        Profile profile = Profile.builder().id(1L).accountId(existing.getId()).build();
        stubOwnership(existing);
        when(profileRepositoryPort.findByAccountId(existing.getId())).thenReturn(java.util.List.of(profile));
        when(subscriptionRepositoryPort.existsActiveByProfileId(profile.getId())).thenReturn(true);

        assertThatThrownBy(() -> updateAccountService.update(ACCOUNT_UUID, updates, EXTERNAL_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("suscripciones activas");

        verify(accountRepositoryPort, never()).save(existing);
    }

    private void stubOwnership(Account existing) {
        User user = User.builder().id(USER_ID).externalId(EXTERNAL_ID).build();
        Vendor vendor = Vendor.builder().id(VENDOR_ID).userId(USER_ID).uuid(UUID.randomUUID()).build();


        when(accountRepositoryPort.findById(ACCOUNT_UUID)).thenReturn(Optional.of(existing));
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Account existingAccount(SaleMode saleMode) {
        return Account.builder()
                .id(10L)
                .uuid(ACCOUNT_UUID)
                .vendorId(VENDOR_ID)
                .serviceId(1L)
                .email("old@example.com")
                .password("old-password")
                .saleMode(saleMode)
                .renewalDate(LocalDate.now().plusDays(10))
                .build();
    }

    private Account updatePayload(SaleMode saleMode) {
        return Account.builder()
                .email("updated@example.com")
                .password("updated-password")
                .saleMode(saleMode)
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Premium")
                .build();
    }
}
