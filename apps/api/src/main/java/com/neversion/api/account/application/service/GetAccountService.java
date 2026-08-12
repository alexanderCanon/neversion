package com.neversion.api.account.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.neversion.api.account.application.port.in.GetAccountUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountDetailResponse;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.AccountSummaryResponse;
import com.neversion.api.account.infrastructure.adapters.in.rest.dto.ProfileSummaryResponse;
import com.neversion.api.account.infrastructure.adapters.in.rest.mapper.AccountMapper;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-028: Account detail with profiles and summary counters.
 * Also implements existing getById / getByServiceId / getAll (unchanged).
 */
@Service
public class GetAccountService implements GetAccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final AccountMapper accountMapper;

    public GetAccountService(AccountRepositoryPort accountRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            AccountMapper accountMapper) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.accountMapper = accountMapper;
    }

    // ─── Existing methods (unchanged) ─────────────────────────────────────────

    @Override
    public Account getById(UUID uuid) {
        return accountRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + uuid));
    }

    @Override
    public List<Account> getByServiceId(Long serviceId) {
        return accountRepositoryPort.findByServiceId(serviceId);
    }

    @Override
    public List<Account> getAll() {
        return accountRepositoryPort.findAll();
    }

    // ─── US-028: Detail ───────────────────────────────────────────────────────

    @Override
    public AccountDetailResponse getDetail(UUID uuid, String callerExternalId) {
        Account account = accountRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + uuid));

        // Ownership check (BR-US028-01)
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(account.getVendorId())) {
            throw new AccessDeniedException("Access denied: you do not own account " + uuid);
        }

        List<Profile> profiles = profileRepositoryPort.findByAccountId(account.getId());

        // Build counters grouped by status
        Map<ProfileStatus, Long> counts = profiles.stream()
                .collect(Collectors.groupingBy(Profile::getStatus, Collectors.counting()));

        AccountSummaryResponse summary = AccountSummaryResponse.builder()
                .total(profiles.size())
                .available(countOf(counts, ProfileStatus.AVAILABLE))
                .active(countOf(counts, ProfileStatus.ACTIVE))
                .reserved(countOf(counts, ProfileStatus.RESERVED))
                .occupied(countOf(counts, ProfileStatus.OCCUPIED))
                .blocked(countOf(counts, ProfileStatus.BLOCKED))
                .expired(countOf(counts, ProfileStatus.EXPIRED))
                .build();

        List<ProfileSummaryResponse> profileSummaries = profiles.stream()
                .map(p -> ProfileSummaryResponse.builder()
                        .id(p.getUuid())
                        .name(p.getName())
                        .pin(p.getPin())
                        .notes(p.getNotes())
                        .isOwner(p.getIsOwner())
                        .status(p.getStatus())
                        .build())
                .toList();

        return AccountDetailResponse.builder()
                .account(accountMapper.toResponse(account))
                .profiles(profileSummaries)
                .summary(summary)
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private int countOf(Map<ProfileStatus, Long> counts, ProfileStatus status) {
        return counts.getOrDefault(status, 0L).intValue();
    }

    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor record not found for user: " + user.getExternalId()))
                .getId();
    }
}
