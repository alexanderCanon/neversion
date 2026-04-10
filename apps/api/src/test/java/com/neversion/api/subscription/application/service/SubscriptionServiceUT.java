package com.neversion.api.subscription.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.exception.AccountOverbookingException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;

/**
 * Unit tests for SubscriptionService (CU-A05).
 * Validates: UUID→Long resolution for Profile and Client,
 * anti-overbooking guard (BR-04), and status defaulting.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService Unit Tests")
class SubscriptionServiceUT {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Mock
    private ProfileRepositoryPort profileRepositoryPort;

    @Mock
    private ClientRepositoryPort clientRepositoryPort;

    private SubscriptionService subscriptionService;

    private static final UUID PROFILE_UUID = UUID.randomUUID();
    private static final UUID CLIENT_UUID = UUID.randomUUID();
    private static final Long PROFILE_ID = 10L;
    private static final Long CLIENT_ID = 20L;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                subscriptionRepositoryPort, profileRepositoryPort, clientRepositoryPort);
    }

    private Profile buildProfile() {
        return Profile.builder()
                .id(PROFILE_ID)
                .uuid(PROFILE_UUID)
                .accountId(1L)
                .name("Profile 1")
                .pin("1234")
                .isOwner(false)
                .build();
    }

    private Client buildClient() {
        return Client.builder()
                .id(CLIENT_ID)
                .uuid(CLIENT_UUID)
                .name("Juan Pérez")
                .phone("55551234")
                .email("juan@example.com")
                .build();
    }

    private Subscription buildInput() {
        return Subscription.builder()
                .profileUuid(PROFILE_UUID)
                .clientUuid(CLIENT_UUID)
                .startDate(LocalDate.now())
                .paymentDueDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Nested
    @DisplayName("assign()")
    class Assign {

        @Test
        @DisplayName("should resolve UUIDs, set ACTIVE status, and persist subscription")
        void shouldAssignSuccessfully() {
            // Given
            Subscription input = buildInput();
            Profile profile = buildProfile();
            Client client = buildClient();

            Subscription persisted = Subscription.builder()
                    .id(1L)
                    .uuid(UUID.randomUUID())
                    .profileId(PROFILE_ID)
                    .clientId(CLIENT_ID)
                    .startDate(LocalDate.now())
                    .paymentDueDate(LocalDate.now().plusDays(30))
                    .status(SubStatus.ACTIVE)
                    .build();

            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile));
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(false);
            when(subscriptionRepositoryPort.save(any(Subscription.class))).thenReturn(persisted);

            // When
            Subscription result = subscriptionService.assign(input);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SubStatus.ACTIVE);
            assertThat(result.getProfileId()).isEqualTo(PROFILE_ID);
            assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
            verify(subscriptionRepositoryPort).save(any(Subscription.class));
        }

        @Test
        @DisplayName("should throw AccountOverbookingException when profile already has active subscription (BR-04)")
        void shouldThrowOverbooking_whenProfileAlreadyActive() {
            // Given
            Subscription input = buildInput();
            Profile profile = buildProfile();
            Client client = buildClient();

            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile));
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.of(client));
            when(subscriptionRepositoryPort.existsActiveByProfileId(PROFILE_ID)).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> subscriptionService.assign(input))
                    .isInstanceOf(AccountOverbookingException.class)
                    .hasMessageContaining("already has an active subscription");

            verify(subscriptionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when profile not found")
        void shouldThrowNotFound_whenProfileNotFound() {
            // Given
            Subscription input = buildInput();

            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> subscriptionService.assign(input))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profile not found");

            verify(subscriptionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void shouldThrowNotFound_whenClientNotFound() {
            // Given
            Subscription input = buildInput();
            Profile profile = buildProfile();

            when(profileRepositoryPort.findById(PROFILE_UUID)).thenReturn(Optional.of(profile));
            when(clientRepositoryPort.findById(CLIENT_UUID)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> subscriptionService.assign(input))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client not found");

            verify(subscriptionRepositoryPort, never()).save(any());
        }
    }
}
