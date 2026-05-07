package com.neversion.api.assignment.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;

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
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

@SpringBootTest
@Transactional
@DisplayName("Assignment repository integration tests")
class AssignmentRepositoryIT extends BaseIntegrationTest {

    @Autowired private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Autowired private ServiceRepositoryPort serviceRepositoryPort;
    @Autowired private AccountRepositoryPort accountRepositoryPort;
    @Autowired private ProfileRepositoryPort profileRepositoryPort;
    @Autowired private ClientRepositoryPort clientRepositoryPort;
    @Autowired private OrderRepositoryPort orderRepositoryPort;

    @Test
    @DisplayName("subscription_shouldPersistOrderIdAndEndDate_andFindByOrderId")
    void subscription_shouldPersistOrderIdAndEndDate_andFindByOrderId() {
        Fixture fixture = createFixture();
        Order order = orderRepositoryPort.save(Order.builder()
                .clientId(fixture.client().getId())
                .status(OrderStatus.VALIDATED)
                .approvedAt(Instant.parse("2026-04-28T12:00:00Z"))
                .build());

        Subscription saved = subscriptionRepositoryPort.save(Subscription.builder()
                .clientId(fixture.client().getId())
                .profileId(fixture.profile().getId())
                .orderId(order.getId())
                .startDate(LocalDate.of(2026, 4, 28))
                .endDate(LocalDate.of(2026, 5, 28))
                .paymentDueDate(LocalDate.of(2026, 5, 28))
                .monthsPaid(1L)
                .status(SubStatus.ACTIVE)
                .build());

        assertThat(saved.getOrderId()).isEqualTo(order.getId());
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 28));
        assertThat(subscriptionRepositoryPort.findByOrderId(order.getId()))
                .isPresent()
                .get()
                .extracting(Subscription::getUuid)
                .isEqualTo(saved.getUuid());
    }

    @Test
    @DisplayName("manualAssignment_shouldPersistSubscriptionWithNullOrderId")
    void manualAssignment_shouldPersistSubscriptionWithNullOrderId() {
        Fixture fixture = createFixture();

        Subscription saved = subscriptionRepositoryPort.save(Subscription.builder()
                .clientId(fixture.client().getId())
                .profileId(fixture.profile().getId())
                .orderId(null)
                .startDate(LocalDate.of(2026, 4, 29))
                .endDate(LocalDate.of(2026, 5, 29))
                .paymentDueDate(LocalDate.of(2026, 5, 29))
                .monthsPaid(1L)
                .status(SubStatus.ACTIVE)
                .build());

        assertThat(saved.getOrderId()).isNull();
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 29));
    }

    private Fixture createFixture() {
        Service service = serviceRepositoryPort.save(Service.builder()
                .name("AssignmentIT-" + System.nanoTime())
                .maxProfiles(5)
                .category(CategoryType.STREAMING)
                .durationDays(30)
                .build());

        Account account = accountRepositoryPort.save(Account.builder()
                .serviceId(service.getId())
                .email("assignment-it-" + System.nanoTime() + "@example.com")
                .password("secret")
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Premium")
                .saleMode(SaleMode.BY_PROFILE)
                .build());

        Profile profile = profileRepositoryPort.save(Profile.builder()
                .accountId(account.getId())
                .name("Assignment IT")
                .pin("1234")
                .isOwner(false)
                .status(ProfileStatus.AVAILABLE)
                .build());

        Client client = clientRepositoryPort.save(Client.builder()
                .name("Assignment Client")
                .phone("55512345678")
                .email("assignment-client-" + System.nanoTime() + "@example.com")
                .build());

        return new Fixture(client, profile);
    }

    private record Fixture(Client client, Profile profile) {
    }
}
