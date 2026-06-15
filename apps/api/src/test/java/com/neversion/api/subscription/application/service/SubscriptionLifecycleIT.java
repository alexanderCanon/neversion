package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.AccountOverbookingException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.subscription.application.port.in.AssignSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.RevokeSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.UpdateSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Phase 1 safety-net integration tests (tech-debt remediation A1).
 * <p>
 * Validates that each subscription state transition keeps the related
 * {@code profiles} / {@code accounts} inventory consistent — the class of bug
 * found on 2026-06-15 where {@code suspend()} left the profile untouched.
 * <p>
 * These tests exercise the real application services against a Testcontainers
 * PostgreSQL instance, so they MUST run outside the sandbox (Docker required).
 */
@SpringBootTest
@Transactional
@DisplayName("Subscription lifecycle inventory-consistency integration tests")
class SubscriptionLifecycleIT extends BaseIntegrationTest {

    @Autowired
    private UpdateSubscriptionUseCase updateSubscriptionUseCase;
    @Autowired
    private RevokeSubscriptionUseCase revokeSubscriptionUseCase;
    @Autowired
    private DetectExpiredSubscriptionsUseCase detectExpiredSubscriptionsUseCase;
    @Autowired
    private AssignSubscriptionUseCase assignSubscriptionUseCase;

    @Autowired
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Autowired
    private ProfileRepositoryPort profileRepositoryPort;
    @Autowired
    private AccountRepositoryPort accountRepositoryPort;
    @Autowired
    private ClientRepositoryPort clientRepositoryPort;
    @Autowired
    private ServiceRepositoryPort serviceRepositoryPort;
    @Autowired
    private UserRepositoryPort userRepositoryPort;
    @Autowired
    private VendorRepositoryPort vendorRepositoryPort;

    private Vendor parentVendor;
    private String vendorExternalId;
    private Service parentService;
    private Client parentClient;
    private Account parentAccount;
    private Profile parentProfile;

    @BeforeEach
    void setUp() {
        vendorExternalId = "auth|lifecycle-vendor-" + System.nanoTime();

        User vendorUser = userRepositoryPort.save(
                User.builder()
                        .externalId(vendorExternalId)
                        .role(UserRole.VENDOR)
                        .build());

        parentVendor = vendorRepositoryPort.save(
                Vendor.builder()
                        .userId(vendorUser.getId())
                        .storeName("Lifecycle Vendor " + System.nanoTime())
                        .build());

        parentService = serviceRepositoryPort.save(
                Service.builder()
                        .name("Netflix-" + System.nanoTime())
                        .vendorId(parentVendor.getId())
                        .maxProfiles(5)
                        .details(null)
                        .category(CategoryType.STREAMING)
                        .build());

        parentAccount = accountRepositoryPort.save(
                Account.builder()
                        .serviceId(parentService.getId())
                        .vendorId(parentVendor.getId())
                        .email("lifecycle-" + System.nanoTime() + "@netflix.com")
                        .password("secret123")
                        .renewalDate(LocalDate.now().plusDays(30))
                        .plan("Premium")
                        .saleMode(SaleMode.BY_PROFILE)
                        .build());

        parentProfile = profileRepositoryPort.save(
                Profile.builder()
                        .accountId(parentAccount.getId())
                        .vendorId(parentVendor.getId())
                        .name("Profile 1")
                        .pin("1234")
                        .isOwner(false)
                        .status(ProfileStatus.ACTIVE)
                        .build());

        parentClient = clientRepositoryPort.save(
                Client.builder()
                        .vendorId(parentVendor.getId())
                        .name("Test Client")
                        .phone("55512345678")
                        .email("client-" + System.nanoTime() + "@test.com")
                        .build());
    }

    private Subscription buildSubscription(Long profileId, SubStatus status, LocalDate paymentDueDate) {
        return Subscription.builder()
                .clientId(parentClient.getId())
                .profileId(profileId)
                .serviceId(parentService.getId())
                .startDate(LocalDate.now())
                .paymentDueDate(paymentDueDate)
                .monthsPaid(1L)
                .status(status)
                .notes("Lifecycle test subscription")
                .vendorId(parentVendor.getId())
                .build();
    }

    private ProfileStatus reloadProfileStatus(Long profileId) {
        return profileRepositoryPort.findByInternalId(profileId)
                .orElseThrow()
                .getStatus();
    }

    @Test
    @DisplayName("suspend - should set the profile status to RESERVED")
    void suspend_shouldSetProfileToReserved() {
        Subscription active = subscriptionRepositoryPort.save(
                buildSubscription(parentProfile.getId(), SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        Subscription result = updateSubscriptionUseCase.suspend(active.getUuid());

        assertThat(result.getStatus()).isEqualTo(SubStatus.SUSPENDED);
        assertThat(reloadProfileStatus(parentProfile.getId())).isEqualTo(ProfileStatus.RESERVED);
    }

    @Test
    @DisplayName("revoke (BY_PROFILE) - should release the profile back to AVAILABLE")
    void revoke_byProfile_shouldReleaseProfileToAvailable() {
        Subscription active = subscriptionRepositoryPort.save(
                buildSubscription(parentProfile.getId(), SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        Subscription result = revokeSubscriptionUseCase.revoke(active.getUuid(), vendorExternalId);

        assertThat(result.getStatus()).isEqualTo(SubStatus.CANCELLED);
        assertThat(reloadProfileStatus(parentProfile.getId())).isEqualTo(ProfileStatus.AVAILABLE);
    }

    @Test
    @DisplayName("revoke (FULL_ACCOUNT) - should release every profile and the account")
    void revoke_fullAccount_shouldReleaseAllProfilesAndAccount() {
        Account fullAccount = accountRepositoryPort.save(
                Account.builder()
                        .serviceId(parentService.getId())
                        .vendorId(parentVendor.getId())
                        .email("full-" + System.nanoTime() + "@netflix.com")
                        .password("secret123")
                        .renewalDate(LocalDate.now().plusDays(30))
                        .plan("Premium")
                        .saleMode(SaleMode.FULL_ACCOUNT)
                        .status(AccountStatus.FULL)
                        .build());

        Profile soldProfile = profileRepositoryPort.save(
                Profile.builder()
                        .accountId(fullAccount.getId())
                        .vendorId(parentVendor.getId())
                        .name("Full Profile 1")
                        .pin("1111")
                        .isOwner(true)
                        .status(ProfileStatus.ACTIVE)
                        .build());

        Profile siblingProfile = profileRepositoryPort.save(
                Profile.builder()
                        .accountId(fullAccount.getId())
                        .vendorId(parentVendor.getId())
                        .name("Full Profile 2")
                        .pin("2222")
                        .isOwner(false)
                        .status(ProfileStatus.ACTIVE)
                        .build());

        Subscription active = subscriptionRepositoryPort.save(
                buildSubscription(soldProfile.getId(), SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        Subscription result = revokeSubscriptionUseCase.revoke(active.getUuid(), vendorExternalId);

        assertThat(result.getStatus()).isEqualTo(SubStatus.CANCELLED);
        assertThat(reloadProfileStatus(soldProfile.getId())).isEqualTo(ProfileStatus.AVAILABLE);
        assertThat(reloadProfileStatus(siblingProfile.getId())).isEqualTo(ProfileStatus.AVAILABLE);
        assertThat(accountRepositoryPort.findByInternalId(fullAccount.getId()).orElseThrow().getStatus())
                .isEqualTo(AccountStatus.AVAILABLE);
    }

    @Test
    @DisplayName("detectAndSuspend - should expire the profile of an overdue subscription")
    void detectAndSuspend_shouldExpireProfile() {
        subscriptionRepositoryPort.save(
                buildSubscription(parentProfile.getId(), SubStatus.ACTIVE, LocalDate.now().minusDays(5)));

        int processed = detectExpiredSubscriptionsUseCase.detectAndSuspend();

        assertThat(processed).isGreaterThanOrEqualTo(1);
        assertThat(reloadProfileStatus(parentProfile.getId())).isEqualTo(ProfileStatus.EXPIRED);
    }

    @Test
    @DisplayName("assign - should throw overbooking when the profile already has an active subscription (BR-04)")
    void assign_secondActiveOnSameProfile_shouldThrowOverbooking() {
        subscriptionRepositoryPort.save(
                buildSubscription(parentProfile.getId(), SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        Subscription second = Subscription.builder()
                .profileUuid(parentProfile.getUuid())
                .clientUuid(parentClient.getUuid())
                .startDate(LocalDate.now())
                .paymentDueDate(LocalDate.now().plusDays(30))
                .vendorId(parentVendor.getId())
                .build();

        assertThatThrownBy(() -> assignSubscriptionUseCase.assign(second))
                .isInstanceOf(AccountOverbookingException.class);
    }

    @Test
    @DisplayName("revoke (BY_PROFILE) twice on the same profile - should allow multiple cancelled subscriptions (V30 fix)")
    void revoke_byProfile_twiceOnSameProfile_shouldSucceed() {
        // First assignment and revocation
        Subscription firstActive = subscriptionRepositoryPort.save(
                buildSubscription(parentProfile.getId(), SubStatus.ACTIVE, LocalDate.now().plusDays(30)));
        Subscription firstResult = revokeSubscriptionUseCase.revoke(firstActive.getUuid(), vendorExternalId);
        assertThat(firstResult.getStatus()).isEqualTo(SubStatus.CANCELLED);

        // Second assignment and revocation on the SAME profile
        Subscription secondActive = subscriptionRepositoryPort.save(
                buildSubscription(parentProfile.getId(), SubStatus.ACTIVE, LocalDate.now().plusDays(30)));
        Subscription secondResult = revokeSubscriptionUseCase.revoke(secondActive.getUuid(), vendorExternalId);
        assertThat(secondResult.getStatus()).isEqualTo(SubStatus.CANCELLED);
    }

    @Test
    @DisplayName("findByAccountId - sanity check that profiles are linked to their account")
    void findByAccountId_shouldReturnProfilesOfAccount() {
        List<Profile> profiles = profileRepositoryPort.findByAccountId(parentAccount.getId());
        assertThat(profiles).extracting(Profile::getId).contains(parentProfile.getId());
    }
}