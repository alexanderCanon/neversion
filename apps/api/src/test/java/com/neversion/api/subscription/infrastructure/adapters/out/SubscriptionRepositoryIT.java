package com.neversion.api.subscription.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@SpringBootTest
@Transactional
@DisplayName("SubscriptionRepositoryPort integration tests")
class SubscriptionRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Autowired
    private ServiceRepositoryPort serviceRepositoryPort;

    @Autowired
    private AccountRepositoryPort accountRepositoryPort;

    @Autowired
    private ProfileRepositoryPort profileRepositoryPort;

    @Autowired
    private ClientRepositoryPort clientRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private VendorRepositoryPort vendorRepositoryPort;

    private Vendor parentVendor;
    private Service parentService;
    private Client parentClient;
    private Profile parentProfile;

    @BeforeEach
    void setUp() {
        User vendorUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|subscription-vendor-" + System.nanoTime())
                        .role(UserRole.VENDOR)
                        .build());

        parentVendor = vendorRepositoryPort.save(
                Vendor.builder()
                        .userId(vendorUser.getId())
                        .storeName("Subscription Vendor " + System.nanoTime())
                        .build());

        parentService = serviceRepositoryPort.save(
                Service.builder()
                        .name("Netflix-" + System.nanoTime())
                        .vendorId(parentVendor.getId())
                        .maxProfiles(5)
                        .details(null)
                        .category(CategoryType.STREAMING)
                        .build());

        Account account = accountRepositoryPort.save(
                Account.builder()
                        .serviceId(parentService.getId())
                        .vendorId(parentVendor.getId())
                        .email("sub-test-" + System.nanoTime() + "@netflix.com")
                        .password("secret123")
                        .renewalDate(LocalDate.now().plusDays(30))
                        .plan("Premium")
                        .saleMode(SaleMode.BY_PROFILE)
                        .build());

        parentProfile = profileRepositoryPort.save(
                Profile.builder()
                        .accountId(account.getId())
                        .vendorId(parentVendor.getId())
                        .name("Profile 1")
                        .pin("1234")
                        .isOwner(false)
                        .build());

        parentClient = clientRepositoryPort.save(
                Client.builder()
                        .vendorId(parentVendor.getId())
                        .name("Test Client")
                        .phone("55512345678")
                        .email("client-" + System.nanoTime() + "@test.com")
                        .build());
    }

    private Subscription buildSubscription(SubStatus status, LocalDate paymentDueDate) {
        return buildSubscription(status, paymentDueDate, parentProfile.getId());
    }

    private Subscription buildSubscription(SubStatus status, LocalDate paymentDueDate, Long profileId) {
        return Subscription.builder()
                .clientId(parentClient.getId())
                .profileId(profileId)
                .serviceId(parentService.getId())
                .startDate(LocalDate.now())
                .paymentDueDate(paymentDueDate)
                .monthsPaid(1L)
                .status(status)
                .notes("Test subscription")
                .vendorId(parentVendor.getId())
                .build();
    }

    private Profile createAnotherProfile() {
        return profileRepositoryPort.save(
                Profile.builder()
                        .accountId(parentProfile.getAccountId())
                        .vendorId(parentVendor.getId())
                        .name("Profile " + System.nanoTime())
                        .pin("1234")
                        .isOwner(false)
                        .build());
    }

    @Test
    @DisplayName("save - should persist subscription and assign uuid")
    void save_shouldPersistSubscription_andAssignUuid() {
        // Given
        Subscription subscription = buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30));

        // When
        Subscription saved = subscriptionRepositoryPort.save(subscription);

        // Then
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getClientId()).isEqualTo(parentClient.getId());
        assertThat(saved.getProfileId()).isEqualTo(parentProfile.getId());
        assertThat(saved.getMonthsPaid()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(SubStatus.ACTIVE);
    }

    @Test
    @DisplayName("existsActiveByProfileId - should return true when active subscription exists (BR-04)")
    void existsActiveByProfileId_shouldReturnTrue_whenActiveSubscriptionExists() {
        // Given
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        // When
        boolean exists = subscriptionRepositoryPort.existsActiveByProfileId(parentProfile.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsActiveByProfileId - should return false when no active subscription")
    void existsActiveByProfileId_shouldReturnFalse_whenNoActiveSubscription() {
        // Given
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.CANCELLED, LocalDate.now().plusDays(30)));

        // When
        boolean exists = subscriptionRepositoryPort.existsActiveByProfileId(parentProfile.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByStatus - should return only matching status")
    void findByStatus_shouldReturnOnlyMatchingStatus() {
        // Given
        Profile anotherProfile = createAnotherProfile();
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.SUSPENDED, LocalDate.now().plusDays(10), anotherProfile.getId()));

        // When
        List<Subscription> activeList = subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE);
        List<Subscription> suspendedList = subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED);

        // Then
        assertThat(activeList).allMatch(s -> s.getStatus() == SubStatus.ACTIVE);
        assertThat(suspendedList).allMatch(s -> s.getStatus() == SubStatus.SUSPENDED);
    }

    @Test
    @DisplayName("findByVendorIdFiltered - should return vendor subscriptions when optional filters are null")
    void findByVendorIdFiltered_nullFilters_shouldReturnVendorSubscriptions() {
        Subscription saved = subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        List<Subscription> result = subscriptionRepositoryPort.findByVendorIdFiltered(
                parentVendor.getId(), null, null);

        assertThat(result)
                .extracting(Subscription::getUuid)
                .contains(saved.getUuid());
    }

    @Test
    @DisplayName("findByVendorIdFiltered - should filter by status when provided")
    void findByVendorIdFiltered_statusFilter_shouldReturnMatchingSubscriptions() {
        Profile anotherProfile = createAnotherProfile();
        Subscription active = subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));
        subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.SUSPENDED, LocalDate.now().plusDays(10), anotherProfile.getId()));

        List<Subscription> result = subscriptionRepositoryPort.findByVendorIdFiltered(
                parentVendor.getId(), null, SubStatus.ACTIVE);

        assertThat(result)
                .extracting(Subscription::getUuid)
                .contains(active.getUuid());
        assertThat(result).allMatch(subscription -> subscription.getStatus() == SubStatus.ACTIVE);
    }

    @Test
    @DisplayName("findByVendorIdFiltered - should filter by service when provided")
    void findByVendorIdFiltered_serviceFilter_shouldReturnMatchingSubscriptions() {
        Subscription saved = subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        List<Subscription> result = subscriptionRepositoryPort.findByVendorIdFiltered(
                parentVendor.getId(), parentService.getId(), null);

        assertThat(result)
                .extracting(Subscription::getUuid)
                .contains(saved.getUuid());
    }

    @Test
    @DisplayName("findVendorSubscriptionViews - should enrich profile, client and service names in one query")
    void findVendorSubscriptionViews_shouldReturnEnrichedView() {
        Subscription saved = subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        List<SubscriptionListView> views = subscriptionRepositoryPort.findVendorSubscriptionViews(
                parentVendor.getId(), null, null);

        assertThat(views)
                .filteredOn(v -> v.subscriptionUuid().equals(saved.getUuid()))
                .singleElement()
                .satisfies(v -> {
                    assertThat(v.profileUuid()).isEqualTo(parentProfile.getUuid());
                    assertThat(v.profileName()).isEqualTo(parentProfile.getName());
                    assertThat(v.clientUuid()).isEqualTo(parentClient.getUuid());
                    assertThat(v.clientName()).isEqualTo(parentClient.getName());
                    assertThat(v.serviceName()).isEqualTo(parentService.getName());
                    assertThat(v.status()).isEqualTo(SubStatus.ACTIVE);
                });
    }

    @Test
    @DisplayName("findVendorSubscriptionViews - should filter by status")
    void findVendorSubscriptionViews_statusFilter_shouldReturnMatching() {
        Profile anotherProfile = createAnotherProfile();
        Subscription active = subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));
        subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.SUSPENDED, LocalDate.now().plusDays(10), anotherProfile.getId()));

        List<SubscriptionListView> views = subscriptionRepositoryPort.findVendorSubscriptionViews(
                parentVendor.getId(), null, SubStatus.ACTIVE);

        assertThat(views).extracting(SubscriptionListView::subscriptionUuid).contains(active.getUuid());
        assertThat(views).allMatch(v -> v.status() == SubStatus.ACTIVE);
    }

    @Test
    @DisplayName("findVendorSubscriptionViews - should filter by service")
    void findVendorSubscriptionViews_serviceFilter_shouldReturnMatching() {
        Subscription saved = subscriptionRepositoryPort.save(
                buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));

        List<SubscriptionListView> views = subscriptionRepositoryPort.findVendorSubscriptionViews(
                parentVendor.getId(), parentService.getId(), null);

        assertThat(views).extracting(SubscriptionListView::subscriptionUuid).contains(saved.getUuid());
    }

    @Test
    @DisplayName("findOverdue - should return active subscriptions with past due date (BR-10)")
    void findOverdue_shouldReturnActiveSubscriptions_withPastDueDate() {
        // Given
        Subscription overdue = buildSubscription(SubStatus.ACTIVE, LocalDate.now().minusDays(5));
        Subscription saved = subscriptionRepositoryPort.save(overdue);

        // When
        List<Subscription> overdueList = subscriptionRepositoryPort.findOverdue(LocalDate.now());

        // Then
        assertThat(overdueList).isNotEmpty();
        assertThat(overdueList).anyMatch(s -> s.getUuid().equals(saved.getUuid()));
    }

    @Test
    @DisplayName("findOverdue - should not return subscription with future payment due date")
    void findOverdue_shouldNotReturn_futurePaymentDueDate() {
        // Given
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(15)));

        // When
        List<Subscription> overdueList = subscriptionRepositoryPort.findOverdue(LocalDate.now().minusDays(1));

        // Then
        assertThat(overdueList).isEmpty();
    }
}
