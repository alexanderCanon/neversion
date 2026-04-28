package com.neversion.api.account.infrastructure.adapters.in.rest.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountRequest;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountResponse;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;

@Component
public class AccountMapper {

    public Account toDomain(AccountRequest request) {
        return request != null ? Account.builder()
                .email(request.email())
                .password(request.pass())
                .serviceUuid(request.serviceId())   // UUID — resolved to Long by service layer
                .saleMode(request.saleMode())
                .renewalDate(request.renewalDate())
                .plan(request.plan())
                .cost(request.cost())
                .source(request.source())
                .purchasedAt(request.purchasedAt())
                .notes(request.notes())
                .build() : null;
    }

    /**
     * Maps account without profile counts — used for create/update responses (US-022/US-023).
     * Profile counter fields default to 0.
     */
    public AccountResponse toResponse(Account account) {
        return toResponse(account, List.of());
    }

    /**
     * Maps account with profile counters — used for listing view (US-024).
     * Computes available, occupied, blocked counts from the provided profile list.
     */
    public AccountResponse toResponse(Account account, List<Profile> profiles) {
        if (account == null) return null;

        int total     = profiles.size();
        int available = countByStatus(profiles, ProfileStatus.AVAILABLE);
        int occupied  = countByStatus(profiles, ProfileStatus.OCCUPIED)
                      + countByStatus(profiles, ProfileStatus.ACTIVE)
                      + countByStatus(profiles, ProfileStatus.RESERVED);
        int blocked   = countByStatus(profiles, ProfileStatus.BLOCKED);

        return AccountResponse.builder()
                .id(account.getUuid())
                .email(account.getEmail())
                // password intentionally omitted from response (security)
                .serviceId(account.getServiceId())
                .saleMode(account.getSaleMode())
                .status(account.getStatus())
                .renewalDate(account.getRenewalDate())
                .plan(account.getPlan())
                .cost(account.getCost())
                .source(account.getSource())
                .purchasedAt(account.getPurchasedAt())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .totalProfiles(total)
                .availableProfiles(available)
                .occupiedProfiles(occupied)
                .blockedProfiles(blocked)
                .build();
    }

    private int countByStatus(List<Profile> profiles, ProfileStatus status) {
        return (int) profiles.stream()
                .filter(p -> status.equals(p.getStatus()))
                .count();
    }
}
