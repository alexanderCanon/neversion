package com.neversion.api.account.infrastructure.adapters.out;

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
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;

@SpringBootTest
@Transactional
@DisplayName("AccountRepositoryPort integration tests")
class AccountRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private AccountRepositoryPort accountRepositoryPort;

    @Autowired
    private ServiceRepositoryPort serviceRepositoryPort;

    private Service parentService;

    @BeforeEach
    void setUp() {
        parentService = serviceRepositoryPort.save(
                Service.builder()
                        .name("Netflix-" + System.nanoTime())
                        .maxProfiles(5)
                        .details(null)
                        .build());
    }

    private Account buildAccount(String email) {
        return Account.builder()
                .serviceId(parentService.getId())
                .email(email)
                .password("secret123")
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Premium")
                .saleMode(SaleMode.BY_PROFILE)
                .notes("Test account")
                .build();
    }

    @Test
    @DisplayName("save - should persist account with all fields")
    void save_shouldPersistAccount_withAllFields() {
        // Given
        Account account = buildAccount("test@netflix.com");

        // When
        Account saved = accountRepositoryPort.save(account);

        // Then
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@netflix.com");
        assertThat(saved.getPassword()).isEqualTo("secret123");
        assertThat(saved.getPlan()).isEqualTo("Premium");
        assertThat(saved.getSaleMode()).isEqualTo(SaleMode.BY_PROFILE);
        assertThat(saved.getServiceId()).isEqualTo(parentService.getId());
        assertThat(saved.getNotes()).isEqualTo("Test account");
    }

    @Test
    @DisplayName("findById - should return account by uuid")
    void findById_shouldReturnAccount_byUuid() {
        // Given
        Account saved = accountRepositoryPort.save(buildAccount("find@netflix.com"));

        // When
        Optional<Account> found = accountRepositoryPort.findById(saved.getUuid());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("find@netflix.com");
    }

    @Test
    @DisplayName("findByServiceId - should return accounts for service")
    void findByServiceId_shouldReturnAccountsForService() {
        // Given
        accountRepositoryPort.save(buildAccount("a1@netflix.com"));
        accountRepositoryPort.save(buildAccount("a2@netflix.com"));

        // When
        List<Account> accounts = accountRepositoryPort.findByServiceId(parentService.getId());

        // Then
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getEmail)
                .containsExactlyInAnyOrder("a1@netflix.com", "a2@netflix.com");
    }

    @Test
    @DisplayName("findAll - should return all accounts")
    void findAll_shouldReturnAllAccounts() {
        // Given
        accountRepositoryPort.save(buildAccount("all1@netflix.com"));
        accountRepositoryPort.save(buildAccount("all2@netflix.com"));

        // When
        List<Account> accounts = accountRepositoryPort.findAll();

        // Then
        assertThat(accounts).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("deleteById - should remove account")
    void deleteById_shouldRemoveAccount() {
        // Given
        Account saved = accountRepositoryPort.save(buildAccount("delete@netflix.com"));

        // When
        accountRepositoryPort.deleteById(saved.getUuid());

        // Then
        Optional<Account> found = accountRepositoryPort.findById(saved.getUuid());
        assertThat(found).isEmpty();
    }
}
