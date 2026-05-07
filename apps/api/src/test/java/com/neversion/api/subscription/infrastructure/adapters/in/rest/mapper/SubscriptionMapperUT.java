package com.neversion.api.subscription.infrastructure.adapters.in.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase.SubscriptionDetail;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.SubscriptionDetailResponse;

@DisplayName("SubscriptionMapper unit tests")
class SubscriptionMapperUT {

    private final SubscriptionMapper subscriptionMapper = new SubscriptionMapper();

    @Test
    @DisplayName("should expose subscription access credentials in explicit access block")
    void toDetailResponse_shouldExposeAccessBlock() {
        Subscription subscription = Subscription.builder()
                .uuid(UUID.randomUUID())
                .status(SubStatus.ACTIVE)
                .startDate(LocalDate.now())
                .paymentDueDate(LocalDate.now().plusDays(30))
                .priceSold(new BigDecimal("75.00"))
                .discountApplied(BigDecimal.ZERO)
                .saleMode(SaleMode.BY_PROFILE)
                .build();
        Client client = Client.builder()
                .uuid(UUID.randomUUID())
                .name("Juan Perez")
                .email("juan@example.com")
                .build();
        Profile profile = Profile.builder()
                .uuid(UUID.randomUUID())
                .name("Perfil 1")
                .pin("1234")
                .build();
        Account account = Account.builder()
                .uuid(UUID.randomUUID())
                .email("stream@example.com")
                .password("secret")
                .plan("Premium")
                .saleMode(SaleMode.BY_PROFILE)
                .build();
        Service service = Service.builder()
                .uuid(UUID.randomUUID())
                .name("Netflix")
                .build();

        SubscriptionDetailResponse response = subscriptionMapper.toDetailResponse(
                new SubscriptionDetail(subscription, client, profile, account, service, null));

        assertThat(response.access()).isNotNull();
        assertThat(response.access().accountEmail()).isEqualTo("stream@example.com");
        assertThat(response.access().accountPassword()).isEqualTo("secret");
        assertThat(response.access().profileName()).isEqualTo("Perfil 1");
        assertThat(response.access().profilePin()).isEqualTo("1234");
        assertThat(response.access().saleMode()).isEqualTo(SaleMode.BY_PROFILE);
    }
}
