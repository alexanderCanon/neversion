package com.neversion.api.subscription.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase.SubscriptionDetail;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
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
                .paymentDueDate(request.paymentDueDate())
                .priceSold(request.priceSold())
                .discountApplied(request.discountApplied())
                .notes(request.notes())
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
                        profile.getIsOwner(),
                        profile.getStatus()))
                .account(new SubscriptionDetailResponse.AccountSummary(
                        account.getUuid(),
                        account.getEmail(),
                        account.getPlan(),
                        account.getSaleMode(),
                        account.getStatus()))
                .access(new SubscriptionDetailResponse.AccessSummary(
                        account.getEmail(),
                        account.getPassword(),
                        profile.getName(),
                        profile.getPin(),
                        account.getSaleMode()))
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
}
