package com.neversion.api.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;

/**
 * Unit tests for GetAccountService.
 * Validates: getById (UUID), getByServiceId, getAll.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAccountService unit tests")
class GetAccountServiceUT {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    private GetAccountService getAccountService;

    private static final UUID ACCOUNT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getAccountService = new GetAccountService(accountRepositoryPort);
    }

    private Account buildAccount() {
        return Account.builder()
                .id(1L)
                .uuid(ACCOUNT_UUID)
                .email("test@gmail.com")
                .password("pass123")
                .serviceId(1L)
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Familiar")
                .saleMode(SaleMode.BY_PROFILE)
                .build();
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return account when found by UUID")
        void getById_shouldReturnAccount_whenFound() {
            // Given
            Account account = buildAccount();
            when(accountRepositoryPort.findById(ACCOUNT_UUID)).thenReturn(Optional.of(account));

            // When
            Account result = getAccountService.getById(ACCOUNT_UUID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUuid()).isEqualTo(ACCOUNT_UUID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void getById_shouldThrowResourceNotFound_whenNotFound() {
            // Given
            when(accountRepositoryPort.findById(ACCOUNT_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> getAccountService.getById(ACCOUNT_UUID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(ACCOUNT_UUID.toString());
        }
    }

    @Nested
    @DisplayName("getByServiceId")
    class GetByServiceId {

        @Test
        @DisplayName("should delegate to repository")
        void getByServiceId_shouldDelegateToRepository() {
            // Given
            List<Account> accounts = List.of(buildAccount());
            when(accountRepositoryPort.findByServiceId(1L)).thenReturn(accounts);

            // When
            List<Account> result = getAccountService.getByServiceId(1L);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all accounts")
        void getAll_shouldReturnAllAccounts() {
            // Given
            List<Account> accounts = List.of(buildAccount());
            when(accountRepositoryPort.findAll()).thenReturn(accounts);

            // When
            List<Account> result = getAccountService.getAll();

            // Then
            assertThat(result).hasSize(1);
        }
    }
}
