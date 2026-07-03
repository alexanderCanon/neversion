package com.neversion.api.subscription.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase.SubscriptionDetail;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.BatchCreateManualSubscriptionRequest;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.BatchCreateSubscriptionsResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.BatchCreateSubscriptionsResponse.BatchItemResult;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.BatchCreateSubscriptionsResponse.BatchItemStatus;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.CreateManualSubscriptionRequest;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.CreateSubscriptionRequest;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.SubscriptionDetailResponse;
import com.neversion.api.subscription.infrastructure.adapters.in.rest.dto.SubscriptionResponse;

@Component
public class SubscriptionMapper {

    public Subscription toDomain(CreateSubscriptionRequest request) {
        if (request == null) return null;

        // UUIDs from request are resolved internally in the use case —
        // we pass them through as lookup keys; the service resolves Long IDs
        return Subscription.builder()
                .profileUuid(request.profileId())
                .clientUuid(request.clientId())
                .accountUuid(request.accountId())
                .startDate(request.startDate())
                .paymentDueDate(request.paymentDueDate())
                .notes(request.notes())
                .build();
    }

    public Subscription toDomain(CreateManualSubscriptionRequest request) {
        if (request == null) return null;

        return Subscription.builder()
                .clientUuid(request.clientId())
                .profileUuid(request.profileId())
                .serviceUuid(request.serviceId())
                .startDate(request.startDate())
                .paymentDueDate(request.paymentDueDate())
                .priceSold(request.priceSold())
                .discountApplied(request.discountApplied())
                .notes(request.notes())
                .build();
    }

    public BatchCreateSubscriptionsUseCase.BatchCommand toCommand(
            BatchCreateManualSubscriptionRequest request) {
        if (request == null) return null;

        var items = request.items().stream()
                .map(item -> new BatchCreateSubscriptionsUseCase.BatchItemCommand(
                        item.serviceId(),
                        item.quantity(),
                        item.priceSold(),
                        item.profileId()))
                .toList();

        return new BatchCreateSubscriptionsUseCase.BatchCommand(
                request.clientId(),
                items,
                request.discountApplied(),
                request.paymentDueDate(),
                request.notes(),
                request.sendNotification());
    }

    public BatchCreateSubscriptionsResponse toBatchResponse(
            BatchCreateSubscriptionsUseCase.BatchResult result) {
        if (result == null) return null;

        var itemResults = result.results().stream()
                .map(r -> BatchItemResult.builder()
                        .serviceId(r.serviceUuid())
                        .status(r.success() ? BatchItemStatus.SUCCESS : BatchItemStatus.FAILED)
                        .subscriptionId(r.subscriptionUuid())
                        .errorMessage(r.errorMessage())
                        .build())
                .toList();

        return BatchCreateSubscriptionsResponse.builder()
                .totalRequested(result.totalRequested())
                .successCount(result.successCount())
                .failedCount(result.failedCount())
                .results(itemResults)
                .build();
    }

    public SubscriptionResponse toResponse(Subscription subscription) {
        return subscription != null ? SubscriptionResponse.builder()
                .id(subscription.getUuid())
                .profileId(subscription.getProfileUuid())
                .clientId(subscription.getClientUuid())
                .accountId(subscription.getAccountUuid())
                .serviceName(null)
                .clientName(null)
                .profileName(null)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .paymentDueDate(subscription.getPaymentDueDate())
                .monthsPaid(subscription.getMonthsPaid())
                .notes(subscription.getNotes())
                .createdAt(subscription.getCreatedAt())
                .build() : null;
    }

    public SubscriptionResponse toListResponse(SubscriptionListView view) {
        return view != null ? SubscriptionResponse.builder()
                .id(view.subscriptionUuid())
                .profileId(view.profileUuid())
                .clientId(view.clientUuid())
                .accountId(view.accountUuid())
                .serviceName(view.serviceName())
                .clientName(view.clientName())
                .profileName(view.profileName())
                .status(view.status())
                .startDate(view.startDate())
                .endDate(view.endDate())
                .paymentDueDate(view.paymentDueDate())
                .monthsPaid(view.monthsPaid())
                .notes(view.notes())
                .createdAt(view.createdAt())
                .build() : null;
    }

    public SubscriptionDetailResponse toDetailResponse(SubscriptionDetail detail) {
        if (detail == null) return null;

        Subscription subscription = detail.subscription();
        Client client = detail.client();
        Profile profile = detail.profile();
        Account account = detail.account();
        Service service = detail.service();
        Order order = detail.order();

        return SubscriptionDetailResponse.builder()
                .id(subscription.getUuid())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .paymentDueDate(subscription.getPaymentDueDate())
                .monthsPaid(subscription.getMonthsPaid())
                .notes(subscription.getNotes())
                .createdAt(subscription.getCreatedAt())
                .financialSnapshot(new SubscriptionDetailResponse.FinancialSnapshot(
                        service != null ? service.getUuid() : null,
                        service != null ? service.getName() : null,
                        subscription.getPriceSold(),
                        subscription.getDiscountApplied(),
                        subscription.getSaleMode()))
                .client(new SubscriptionDetailResponse.ClientSummary(
                        client.getUuid(),
                        client.getName(),
                        client.getEmail(),
                        client.getPhone()))
                .profile(new SubscriptionDetailResponse.ProfileSummary(
                        profile.getUuid(),
                        profile.getName(),
                        profile.getPin(),
                        profile.getNotes(),
                        profile.getIsOwner(),
                        profile.getStatus()))
                .account(new SubscriptionDetailResponse.AccountSummary(
                        account.getUuid(),
                        account.getEmail(),
                        account.getPlan(),
                        account.getSaleMode(),
                        account.getStatus()))
                .access(buildAccessSummary(service, account, profile))
                .order(toOrderSummary(order))
                .build();
    }

    private SubscriptionDetailResponse.OrderSummary toOrderSummary(Order order) {
        if (order == null) return null;
        return new SubscriptionDetailResponse.OrderSummary(
                order.getUuid(),
                order.getReservationUuid(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getTotal(),
                order.getDiscount(),
                order.getReceiptUrl(),
                order.getApprovedAt(),
                order.getCreatedAt());
    }

    /**
     * Builds the AccessSummary for a subscription detail response.
     *
     * Spotify Family (BY_PROFILE): each client uses their own personal account or
     * an invitation link. Exposing the master account credentials would compromise
     * the vendor's anchor account. accountEmail and accountPassword are set to null.
     */
    private SubscriptionDetailResponse.AccessSummary buildAccessSummary(
            com.neversion.api.service.domain.model.Service service,
            Account account,
            com.neversion.api.profile.domain.model.Profile profile) {

        boolean isSpotifyByProfile = service != null
                && "Spotify".equalsIgnoreCase(service.getName())
                && account.getSaleMode() == SaleMode.BY_PROFILE;

        return new SubscriptionDetailResponse.AccessSummary(
                isSpotifyByProfile ? null : account.getEmail(),
                isSpotifyByProfile ? null : account.getPassword(),
                // For Spotify slots, profileName carries the invitation link / personal email.
                profile.getNotes() != null ? profile.getNotes() : profile.getName(),
                profile.getPin(),
                account.getSaleMode());
    }
}
