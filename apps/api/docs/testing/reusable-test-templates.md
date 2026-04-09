# Reusable Test Templates

These templates and utilities serve as a starting point for specialized agents or developers implementing the tests defined in the `phased-testing-plan.md`.

## 1. Domain Object Builder Fixtures (Test Data Pattern)

Use static factory methods or Lombok builders to generate valid domain objects for tests without cluttering the test logic.

```java
public class SubscriptionFixture {

    public static Subscription.SubscriptionBuilder activeSubscription() {
        return Subscription.builder()
            .id(1L)
            .uuid(UUID.randomUUID())
            .clientId(100L)
            .profileId(200L)
            .startDate(LocalDate.now())
            .paymentDueDate(LocalDate.now().plusMonths(1))
            .monthsPaid(1L)
            .status(SubStatus.ACTIVE);
    }
}
```

## 2. Standard Application Use Case Unit Test (`*UT.java`)

Demonstrates isolating business logic using Mockito in the application layer.

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignSubscriptionServiceUT {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private ProfileRepositoryPort profileRepository;

    @InjectMocks
    private AssignSubscriptionService service;

    @Test
    void givenActiveSubscriptionOnProfile_whenAssign_thenThrowsAccountOverbookingException() {
        // Arrange
        UUID profileId = UUID.randomUUID();
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(ProfileFixture.defaultProfile().build()));
        when(subscriptionRepository.hasActiveSubscription(anyLong())).thenReturn(true);

        Subscription request = SubscriptionFixture.activeSubscription().profileUuid(profileId).build();

        // Act & Assert
        assertThatThrownBy(() -> service.assign(request))
            .isInstanceOf(AccountOverbookingException.class)
            .hasMessageContaining("already has an active subscription");

        verify(subscriptionRepository, never()).save(any());
    }
}
```

## 3. Persistence Integration Test (`*IT.java`)

Demonstrates testing repositories against a real PostgreSQL Testcontainer.

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpringDataSubscriptionRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private SpringDataSubscriptionRepository repository;

    @Test
    void givenActiveSubscription_whenFindByStatus_thenReturned() {
        // ... persistence logic
    }
}
```
