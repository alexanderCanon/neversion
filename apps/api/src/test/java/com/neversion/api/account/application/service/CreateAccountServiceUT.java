package com.neversion.api.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Unit tests for CreateAccountService (US-022).
 * Validates: renewal date guard, service lookup, JWT vendorId resolution,
 * profile auto-generation (BR-01), and FULL_ACCOUNT vs BY_PROFILE branching.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAccountService unit tests")
class CreateAccountServiceUT {

    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private VendorRepositoryPort vendorRepositoryPort;
    @Mock private ProfileUseCase profileUseCase;

    private CreateAccountService createAccountService;

    private static final Long    SERVICE_ID    = 1L;
    private static final UUID    SERVICE_UUID  = UUID.randomUUID();
    private static final Long    VENDOR_ID     = 42L;
    private static final Long    USER_ID       = 7L;
    private static final String  EXTERNAL_ID   = "supabase-uuid-test";

    @BeforeEach
    void setUp() {
        createAccountService = new CreateAccountService(
                accountRepositoryPort, serviceRepositoryPort,
                userRepositoryPort, vendorRepositoryPort, profileUseCase);
    }

    /** Stubs the Supabase JWT → User → Vendor chain. Call in tests that reach vendorId resolution. */
    private void stubJwtChain() {
        User user = User.builder().id(USER_ID).build();
        Vendor vendor = Vendor.builder().id(VENDOR_ID).build();
        when(userRepositoryPort.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(user));
        when(vendorRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(vendor));
    }

    private Account buildAccount(SaleMode saleMode) {
        return Account.builder()
                .email("netflix@example.com")
                .password("pass123")
                .serviceUuid(SERVICE_UUID)          // UUID — as sent by frontend
                .renewalDate(LocalDate.now().plusDays(30))
                .plan("Premium")
                .saleMode(saleMode)
                .build();
    }

    private Service buildService(Integer maxProfiles) {
        return Service.builder()
                .id(SERVICE_ID)
                .uuid(SERVICE_UUID)
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
            stubJwtChain();
            Account account = buildAccount(SaleMode.BY_PROFILE);
            Account saved   = buildAccount(SaleMode.BY_PROFILE);
            saved.setId(10L);
            saved.setVendorId(VENDOR_ID);
            saved.setMaxProfiles(5);

            Service service = buildService(5);

            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(accountRepositoryPort.save(any(Account.class))).thenReturn(saved);

            // When
            Account result = createAccountService.create(account, EXTERNAL_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            verify(profileUseCase).generateProfilesForAccount(10L, 5, VENDOR_ID);
        }

        @Test
        @DisplayName("should save FULL_ACCOUNT and generate one owner profile")
        void create_shouldSaveFullAccountAndGenerateOwnerProfile() {
            // Given
            stubJwtChain();
            Account account = buildAccount(SaleMode.FULL_ACCOUNT);
            Account saved   = buildAccount(SaleMode.FULL_ACCOUNT);
            saved.setId(11L);
            saved.setVendorId(VENDOR_ID);

            Service service = buildService(5);

            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(accountRepositoryPort.save(any(Account.class))).thenReturn(saved);

            // When
            Account result = createAccountService.create(account, EXTERNAL_ID);

            // Then
            assertThat(result).isNotNull();
            verify(profileUseCase).generateProfilesForAccount(11L, 1, VENDOR_ID);
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

            // When / Then — renewal guard fires before JWT resolution
            assertThatThrownBy(() -> createAccountService.create(account, EXTERNAL_ID))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Renewal date is required");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when service not found")
        void create_shouldThrowResourceNotFound_whenServiceNotFound() {
            // Given
            stubJwtChain();
            Account account = buildAccount(SaleMode.BY_PROFILE);
            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> createAccountService.create(account, EXTERNAL_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Service not found");
        }

        @Test
        @DisplayName("should default to 1 profile when service.maxProfiles is null (BR-01)")
        void create_shouldDefaultTo1Profile_whenMaxProfilesIsNull() {
            // Given
            stubJwtChain();
            Account account = buildAccount(SaleMode.BY_PROFILE);
            Account saved   = buildAccount(SaleMode.BY_PROFILE);
            saved.setId(12L);
            saved.setVendorId(VENDOR_ID);

            Service service = buildService(null);

            when(serviceRepositoryPort.findById(SERVICE_UUID)).thenReturn(Optional.of(service));
            when(accountRepositoryPort.save(any(Account.class))).thenReturn(saved);

            // When
            createAccountService.create(account, EXTERNAL_ID);

            // Then
            verify(profileUseCase).generateProfilesForAccount(12L, 1, VENDOR_ID);
        }
    }
}
