package com.neversion.api.assignment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliverAccessService Unit Tests")
class DeliverAccessServiceUT {

    @Mock private ProfileRepositoryPort profileRepositoryPort;
    @Mock private AccountRepositoryPort accountRepositoryPort;
    @Mock private ServiceRepositoryPort serviceRepositoryPort;
    @Mock private ClientRepositoryPort clientRepositoryPort;
    @Mock private NotificationLogPort notificationLogPort;

    @Test
    @DisplayName("deliver_shouldRecordAccessDeliveredNotification_withFullPayload")
    void deliver_shouldRecordAccessDeliveredNotification_withFullPayload() {
        DeliverAccessService service = newService();
        UUID subscriptionUuid = UUID.randomUUID();

        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L)
                .accountId(20L)
                .name("Casa")
                .pin("1234")
                .build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L)
                .serviceId(30L)
                .email("stream@example.com")
                .password("secret")
                .build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .name("Netflix")
                .build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .name("Ana")
                .email("ana@example.com")
                .build()));

        service.deliver(Subscription.builder()
                .uuid(subscriptionUuid)
                .profileId(10L)
                .clientId(40L)
                .endDate(LocalDate.of(2026, 5, 28))
                .build());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("ana@example.com"), payloadCaptor.capture(),
                eq("subscription"), any(), eq("access_delivered"));
        assertThat(payloadCaptor.getValue()).contains(
                "\"subscriptionId\":\"" + subscriptionUuid + "\"",
                "\"serviceName\":\"Netflix\"",
                "\"accountEmail\":\"stream@example.com\"",
                "\"accountPassword\":\"secret\"",
                "\"profileName\":\"Casa\"",
                "\"pin\":\"1234\"",
                "\"endDate\":\"2026-05-28\"",
                "\"clientName\":\"Ana\"");
    }

    @Test
    @DisplayName("deliver_shouldOmitPin_whenProfileHasNoPin")
    void deliver_shouldOmitPin_whenProfileHasNoPin() {
        DeliverAccessService service = newService();

        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L)
                .accountId(20L)
                .name("Casa")
                .build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L)
                .serviceId(30L)
                .email("stream@example.com")
                .password("secret")
                .build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .name("Netflix")
                .build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .name("Ana")
                .email("ana@example.com")
                .build()));

        service.deliver(Subscription.builder()
                .uuid(UUID.randomUUID())
                .profileId(10L)
                .clientId(40L)
                .endDate(LocalDate.of(2026, 5, 28))
                .build());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("ana@example.com"), payloadCaptor.capture(),
                eq("subscription"), any(), eq("access_delivered"));
        assertThat(payloadCaptor.getValue()).doesNotContain("\"pin\"");
    }

    @Test
    @DisplayName("deliver_shouldOmitMasterCredentials_whenSpotifyByProfile")
    void deliver_shouldOmitMasterCredentials_whenSpotifyByProfile() {
        DeliverAccessService svc = newService();
        UUID subscriptionUuid = UUID.randomUUID();

        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L)
                .accountId(20L)
                .name("Perfil 1")
                .pin(null)
                .build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L)
                .serviceId(30L)
                .email("master@spotify.com")
                .password("masterSecret")
                .saleMode(SaleMode.BY_PROFILE)
                .build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .name("Spotify")
                .build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .name("Carlos")
                .email("carlos@example.com")
                .build()));

        svc.deliver(Subscription.builder()
                .uuid(subscriptionUuid)
                .profileId(10L)
                .clientId(40L)
                .endDate(LocalDate.of(2026, 12, 31))
                .build());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("carlos@example.com"),
                payloadCaptor.capture(), eq("subscription"), any(), eq("access_delivered"));

        String payload = payloadCaptor.getValue();
        assertThat(payload)
                .contains("\"serviceName\":\"Spotify\"")
                .contains("\"subscriptionId\":\"" + subscriptionUuid + "\"")
                .doesNotContain("\"accountEmail\"")
                .doesNotContain("\"accountPassword\"");
    }

    @Test
    @DisplayName("deliver_shouldUseProfileNotes_asProfileName_whenSpotifyByProfileAndNotesPresent")
    void deliver_shouldUseProfileNotes_asProfileName_whenSpotifyByProfileAndNotesPresent() {
        DeliverAccessService svc = newService();

        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L)
                .accountId(20L)
                .name("Perfil 1")
                .notes("https://spotify.com/invite/abc123")
                .build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L)
                .serviceId(30L)
                .email("master@spotify.com")
                .password("masterSecret")
                .saleMode(SaleMode.BY_PROFILE)
                .build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L)
                .name("Spotify")
                .build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L)
                .name("Laura")
                .email("laura@example.com")
                .build()));

        svc.deliver(Subscription.builder()
                .uuid(UUID.randomUUID())
                .profileId(10L)
                .clientId(40L)
                .endDate(LocalDate.of(2026, 12, 31))
                .build());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("laura@example.com"),
                payloadCaptor.capture(), eq("subscription"), any(), eq("access_delivered"));

        assertThat(payloadCaptor.getValue())
                .contains("\"profileName\":\"https://spotify.com/invite/abc123\"")
                .doesNotContain("\"accountEmail\"")
                .doesNotContain("\"accountPassword\"");
    }

    private DeliverAccessService newService() {
        return new DeliverAccessService(
                profileRepositoryPort,
                accountRepositoryPort,
                serviceRepositoryPort,
                clientRepositoryPort,
                notificationLogPort,
                new NotificationPayloadWriter(new ObjectMapper().findAndRegisterModules()));
    }
}
