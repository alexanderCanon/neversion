package com.neversion.api.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

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
import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.inventory.domain.model.Service;
import com.neversion.api.inventory.domain.port.out.ServiceRepositoryPort;

/**
 * Unit tests for CreateAccountService (CU-A01).
 * Validates: renewal date guard, service lookup, profile auto-generation (BR-01),
 * and FULL_ACCOUNT vs BY_PROFILE branching.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAccountService unit tests")
class CreateAccountServiceUT {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;

    @Mock
    private ProfileUseCase profileUseCase;

    private CreateAccountService createAccountService;

    private static final Long SERVICE_ID = 1L;

    @BeforeEach
    void setUp() {
        createAccountService = new CreateAccountService(
                accountRepositoryPort, serviceRepositoryPort, profileUseCase);
    }

    private Account buildAccount(SaleMode saleMode) {
        return Account.builder()
                .email("netflix@example.com")
                .password("pass123")
                .serviceId(SERVICE_ID)
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Premium")
                .saleMode(saleMode)
                .build();
    }

    private Service buildService(Integer maxProfiles) {
        return Service.builder()
                .id(SERVICE_ID)
                .name("Netflix")
                .maxProfiles(maxProfiles)
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should save BY_PROFILE account and generate profiles from service.maxProfiles")
        void create_shouldSaveByProfileAccountAndGenerateProfiles() {
            // Given
            Account account = buildAccount(SaleMode.BY_PROFILE);
            Account saved = buildAccount(SaleMode.BY_PROFILE);
            saved.setId(10L);

            Service service = buildService(5);

            when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.of(service));
            when(accountRepositoryPort.save(account)).thenReturn(saved);

            // When
            Account result = createAccountService.create(account);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            verify(profileUseCase).generateProfilesForAccount(10L, 5);
        }

        @Test
        @DisplayName("should save FULL_ACCOUNT without generating profiles")
        void create_shouldSaveFullAccountWithoutProfiles() {
            // Given
            Account account = buildAccount(SaleMode.FULL_ACCOUNT);
            Account saved = buildAccount(SaleMode.FULL_ACCOUNT);
            saved.setId(11L);

            Service service = buildService(5);

            when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.of(service));
            when(accountRepositoryPort.save(account)).thenReturn(saved);

            // When
            Account result = createAccountService.create(account);

            // Then
            assertThat(result).isNotNull();
            verify(profileUseCase, never()).generateProfilesForAccount(any(), any());
        }

        @Test
        @DisplayName("should throw BusinessRuleException when renewal date is null")
        void create_shouldThrowBusinessRuleException_whenRenewalDateIsNull() {
            // Given
            Account account = Account.builder()
                    .email("test@example.com")
                    .password("pass123")
                    .serviceId(SERVICE_ID)
                    .saleMode(SaleMode.BY_PROFILE)
                    .build(); // renewalDate is null

            // When / Then
            assertThatThrownBy(() -> createAccountService.create(account))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Renewal date is required");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when service not found")
        void create_shouldThrowResourceNotFound_whenServiceNotFound() {
            // Given
            Account account = buildAccount(SaleMode.BY_PROFILE);

            when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> createAccountService.create(account))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Service not found");
        }

        @Test
        @DisplayName("should default to 1 profile when service.maxProfiles is null (BR-01)")
        void create_shouldDefaultTo1Profile_whenMaxProfilesIsNull() {
            // Given
            Account account = buildAccount(SaleMode.BY_PROFILE);
            Account saved = buildAccount(SaleMode.BY_PROFILE);
            saved.setId(12L);

            Service service = buildService(null);

            when(serviceRepositoryPort.findByInternalId(SERVICE_ID)).thenReturn(Optional.of(service));
            when(accountRepositoryPort.save(account)).thenReturn(saved);

            // When
            createAccountService.create(account);

            // Then
            verify(profileUseCase).generateProfilesForAccount(12L, 1);
        }
    }
}
