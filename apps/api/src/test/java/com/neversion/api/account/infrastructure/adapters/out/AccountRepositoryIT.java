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
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@SpringBootTest
@Transactional
@DisplayName("AccountRepositoryPort integration tests")
class AccountRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private AccountRepositoryPort accountRepositoryPort;

    @Autowired
    private ServiceRepositoryPort serviceRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private VendorRepositoryPort vendorRepositoryPort;

    private Vendor parentVendor;
    private Service parentService;

    @BeforeEach
    void setUp() {
        User vendorUser = userRepositoryPort.save(
                User.builder()
                        .externalId("auth|account-vendor-" + System.nanoTime())
                        .role(UserRole.VENDOR)
                        .build());

        parentVendor = vendorRepositoryPort.save(
                Vendor.builder()
                        .userId(vendorUser.getId())
                        .storeName("Account Vendor " + System.nanoTime())
                        .build());

        parentService = serviceRepositoryPort.save(
                Service.builder()
                        .name("Netflix-" + System.nanoTime())
                        .vendorId(parentVendor.getId())
                        .maxProfiles(5)
                        .details(null)
                        .category(CategoryType.STREAMING)
                        .build());
    }

    private Account buildAccount(String email) {
        return buildAccount(email, LocalDate.now().plusDays(30));
    }

    private Account buildAccount(String email, LocalDate renewalDate) {
        return Account.builder()
                .serviceId(parentService.getId())
                .vendorId(parentVendor.getId())
                .email(email)
                .password("secret123")
                .renewalDate(renewalDate)
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
    @DisplayName("findByVendorIdFiltered - should filter by service when status is null")
    void findByVendorIdFiltered_serviceFilter_shouldReturnMatchingAccounts() {
        Account saved = accountRepositoryPort.save(buildAccount("service-filter@netflix.com"));

        List<Account> accounts = accountRepositoryPort.findByVendorIdFiltered(
                parentVendor.getId(), parentService.getId(), null);

        assertThat(accounts)
                .extracting(Account::getUuid)
                .contains(saved.getUuid());
    }

    @Test
    @DisplayName("findByVendorIdFiltered - should filter by status when provided")
    void findByVendorIdFiltered_statusFilter_shouldReturnMatchingAccounts() {
        Account saved = accountRepositoryPort.save(buildAccount("status-filter@netflix.com"));

        List<Account> accounts = accountRepositoryPort.findByVendorIdFiltered(
                parentVendor.getId(), null, AccountStatus.AVAILABLE);

        assertThat(accounts)
                .extracting(Account::getUuid)
                .contains(saved.getUuid());
    }

    @Test
    @DisplayName("findByRenewalDate - should return accounts with matching renewal date")
    void findByRenewalDate_shouldReturnAccountsWithMatchingRenewalDate() {
        LocalDate renewalDate = LocalDate.now().plusDays(7);
        Account saved = accountRepositoryPort.save(buildAccount("renewal-date@netflix.com", renewalDate));
        accountRepositoryPort.save(buildAccount("other-renewal-date@netflix.com", renewalDate.plusDays(1)));

        List<Account> accounts = accountRepositoryPort.findByRenewalDate(renewalDate);

        assertThat(accounts)
                .extracting(Account::getUuid)
                .contains(saved.getUuid());
        assertThat(accounts)
                .extracting(Account::getEmail)
                .doesNotContain("other-renewal-date@netflix.com");
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
