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
import com.neversion.api.account.domain.model.enums.ProfileDeliveryType;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountPreference;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliverAccessService Unit Tests")
class DeliverAccessServiceUT {

    @Mock private ProfileRepositoryPort     profileRepositoryPort;
    @Mock private AccountRepositoryPort     accountRepositoryPort;
    @Mock private ServiceRepositoryPort     serviceRepositoryPort;
    @Mock private ClientRepositoryPort      clientRepositoryPort;
    @Mock private NotificationLogPort       notificationLogPort;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void mockNetflixFixture() {
        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L).accountId(20L).name("Casa").pin("1234").build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L).serviceId(30L).email("stream@example.com").password("secret").build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L).name("Netflix").build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L).name("Ana").email("ana@example.com").build()));
    }

    private void mockSpotifyFixture(String profileName, String pin, ProfileDeliveryType deliveryType) {
        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L).accountId(20L).name(profileName).pin(pin).build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L).serviceId(30L).email("master@spotify.com").password("masterSecret")
                .saleMode(SaleMode.BY_PROFILE).profileDeliveryType(deliveryType).build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L).name("Spotify").build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L).name("Laura").email("laura@example.com").build()));
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

    // ── Standard service (Netflix) ────────────────────────────────────────────

    @Test
    @DisplayName("deliver — standard service — should send master credentials and profile slot")
    void deliver_standardService_shouldSendFullCredentials() {
        mockNetflixFixture();
        UUID subscriptionUuid = UUID.randomUUID();

        newService().deliver(Subscription.builder()
                .uuid(subscriptionUuid).profileId(10L).clientId(40L)
                .endDate(LocalDate.of(2026, 5, 28)).build());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("ana@example.com"),
                captor.capture(), eq("subscription"), any(), eq("access_delivered"));

        assertThat(captor.getValue())
                .contains("\"subscriptionId\":\"" + subscriptionUuid + "\"")
                .contains("\"serviceName\":\"Netflix\"")
                .contains("\"accountEmail\":\"stream@example.com\"")
                .contains("\"accountPassword\":\"secret\"")
                .contains("\"profileName\":\"Casa\"")
                .contains("\"pin\":\"1234\"")
                .contains("\"endDate\":\"2026-05-28\"")
                .contains("\"clientName\":\"Ana\"")
                .doesNotContain("\"followUpViaWhatsapp\"");
    }

    @Test
    @DisplayName("deliver — standard service — should omit pin when profile has none")
    void deliver_standardService_shouldOmitPin_whenProfileHasNoPin() {
        when(profileRepositoryPort.findByInternalId(10L)).thenReturn(Optional.of(Profile.builder()
                .id(10L).accountId(20L).name("Casa").build()));
        when(accountRepositoryPort.findByInternalId(20L)).thenReturn(Optional.of(Account.builder()
                .id(20L).serviceId(30L).email("stream@example.com").password("secret").build()));
        when(serviceRepositoryPort.findByInternalId(30L)).thenReturn(Optional.of(Service.builder()
                .id(30L).name("Netflix").build()));
        when(clientRepositoryPort.findByInternalId(40L)).thenReturn(Optional.of(Client.builder()
                .id(40L).name("Ana").email("ana@example.com").build()));

        newService().deliver(Subscription.builder()
                .uuid(UUID.randomUUID()).profileId(10L).clientId(40L)
                .endDate(LocalDate.of(2026, 5, 28)).build());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("ana@example.com"),
                captor.capture(), eq("subscription"), any(), eq("access_delivered"));
        assertThat(captor.getValue()).doesNotContain("\"pin\"");
    }

    // ── Spotify — master credentials always hidden ────────────────────────────

    @Test
    @DisplayName("deliver — Spotify PERSONAL_ACCOUNT — should never expose master credentials")
    void deliver_spotifyByProfile_shouldNeverExposeMasterCredentials() {
        mockSpotifyFixture("Perfil 1", null, ProfileDeliveryType.PERSONAL_ACCOUNT);
        UUID subscriptionUuid = UUID.randomUUID();

        newService().deliver(Subscription.builder()
                .uuid(subscriptionUuid).profileId(10L).clientId(40L)
                .accountPreference(AccountPreference.CUENTA_NUEVA)
                .endDate(LocalDate.of(2026, 12, 31)).build());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("laura@example.com"),
                captor.capture(), eq("subscription"), any(), eq("access_delivered"));

        assertThat(captor.getValue())
                .doesNotContain("\"accountEmail\"")
                .doesNotContain("\"accountPassword\"")
                .doesNotContain("master@spotify.com")
                .doesNotContain("masterSecret");
    }

    // ── Spotify — Cuenta nueva ────────────────────────────────────────────────

    @Test
    @DisplayName("deliver — Spotify cuenta nueva — should send profile credentials without followUpViaWhatsapp")
    void deliver_spotifyCuentaNueva_shouldSendProfileCredentials() {
        mockSpotifyFixture("nuevo@gmail.com", "ClaveSegura1", ProfileDeliveryType.PERSONAL_ACCOUNT);

        newService().deliver(Subscription.builder()
                .uuid(UUID.randomUUID()).profileId(10L).clientId(40L)
                .accountPreference(AccountPreference.CUENTA_NUEVA)
                .endDate(LocalDate.of(2026, 12, 31)).build());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("laura@example.com"),
                captor.capture(), eq("subscription"), any(), eq("access_delivered"));

        String payload = captor.getValue();
        assertThat(payload)
                .contains("\"followUpViaWhatsapp\":false")
                .contains("\"profileName\":\"nuevo@gmail.com\"")
                .contains("\"pin\":\"ClaveSegura1\"")
                .doesNotContain("\"accountEmail\"")
                .doesNotContain("\"accountPassword\"");
    }

    // ── Spotify — Cuenta propia ───────────────────────────────────────────────

    @Test
    @DisplayName("deliver — Spotify cuenta propia — should set followUpViaWhatsapp=true and omit all credentials")
    void deliver_spotifyCuentaPropia_shouldFlagWhatsappFollowUp_andOmitCredentials() {
        mockSpotifyFixture("Perfil 1", null, ProfileDeliveryType.PERSONAL_ACCOUNT);

        newService().deliver(Subscription.builder()
                .uuid(UUID.randomUUID()).profileId(10L).clientId(40L)
                .accountPreference(AccountPreference.CUENTA_PROPIA)
                .endDate(LocalDate.of(2026, 12, 31)).build());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("laura@example.com"),
                captor.capture(), eq("subscription"), any(), eq("access_delivered"));

        String payload = captor.getValue();
        assertThat(payload)
                .contains("\"followUpViaWhatsapp\":true")
                .doesNotContain("\"accountEmail\"")
                .doesNotContain("\"accountPassword\"")
                .doesNotContain("\"profileName\"")
                .doesNotContain("\"pin\"");
    }

    @Test
    @DisplayName("deliver — Spotify sin preferencia — should default to cuenta nueva behaviour")
    void deliver_spotifyNoOrderId_shouldDefaultToCuentaNuevaBehaviour() {
        mockSpotifyFixture("Perfil 1", null, ProfileDeliveryType.PERSONAL_ACCOUNT);

        newService().deliver(Subscription.builder()
                .uuid(UUID.randomUUID()).profileId(10L).clientId(40L)
                .accountPreference(null)
                .endDate(LocalDate.of(2026, 12, 31)).build());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(notificationLogPort).record(eq("ACCESS_DELIVERED"), eq("laura@example.com"),
                captor.capture(), eq("subscription"), any(), eq("access_delivered"));

        assertThat(captor.getValue())
                .contains("\"followUpViaWhatsapp\":false")
                .doesNotContain("\"accountEmail\"")
                .doesNotContain("\"accountPassword\"");
    }
}
