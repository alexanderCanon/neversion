package com.neversion.api.subscription.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

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

    private Client parentClient;
    private Profile parentProfile;

    @BeforeEach
    void setUp() {
        Service service = serviceRepositoryPort.save(
                Service.builder()
                        .name("Netflix-" + System.nanoTime())
                        .maxProfiles(5)
                        .details(null)
                        .build());

        Account account = accountRepositoryPort.save(
                Account.builder()
                        .serviceId(service.getId())
                        .email("sub-test-" + System.nanoTime() + "@netflix.com")
                        .password("secret123")
                        .renewalDate(LocalDate.now().plusDays(30))
                        .plan("Premium")
                        .saleMode(SaleMode.BY_PROFILE)
                        .build());

        parentProfile = profileRepositoryPort.save(
                Profile.builder()
                        .accountId(account.getId())
                        .name("Profile 1")
                        .pin("1234")
                        .isOwner(false)
                        .build());

        parentClient = clientRepositoryPort.save(
                Client.builder()
                        .name("Test Client")
                        .phone("55512345678")
                        .email("client-" + System.nanoTime() + "@test.com")
                        .build());
    }

    private Subscription buildSubscription(SubStatus status, LocalDate paymentDueDate) {
        return Subscription.builder()
                .clientId(parentClient.getId())
                .profileId(parentProfile.getId())
                .startDate(LocalDate.now())
                .paymentDueDate(paymentDueDate)
                .monthsPaid(1L)
                .status(status)
                .notes("Test subscription")
                .build();
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
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.ACTIVE, LocalDate.now().plusDays(30)));
        subscriptionRepositoryPort.save(buildSubscription(SubStatus.SUSPENDED, LocalDate.now().plusDays(10)));

        // When
        List<Subscription> activeList = subscriptionRepositoryPort.findByStatus(SubStatus.ACTIVE);
        List<Subscription> suspendedList = subscriptionRepositoryPort.findByStatus(SubStatus.SUSPENDED);

        // Then
        assertThat(activeList).allMatch(s -> s.getStatus() == SubStatus.ACTIVE);
        assertThat(suspendedList).allMatch(s -> s.getStatus() == SubStatus.SUSPENDED);
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
