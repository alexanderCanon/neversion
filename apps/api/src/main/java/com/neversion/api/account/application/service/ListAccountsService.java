package com.neversion.api.account.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.neversion.api.account.application.port.in.ListAccountsUseCase;
import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

/**
 * US-024: Returns the vendor's account list with optional filters.
 * Resolves vendorUuid → internal vendorId, then applies serviceUuid/status filters.
 */
@Service
public class ListAccountsService implements ListAccountsUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public ListAccountsService(AccountRepositoryPort accountRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public List<Account> listByVendor(UUID vendorUuid, UUID serviceUuid, AccountStatus status, String callerExternalId) {
        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(vendor.getId())) {
            throw new AccessDeniedException("Access denied: you do not own vendor " + vendorUuid);
        }

        // Resolve optional serviceUuid → internal serviceId
        Long serviceId = null;
        if (serviceUuid != null) {
            serviceId = serviceRepositoryPort.findById(serviceUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceUuid))
                    .getId();
        }

        if (serviceId == null && status == null) {
            return accountRepositoryPort.findByVendorId(vendor.getId());
        }
        return accountRepositoryPort.findByVendorIdFiltered(vendor.getId(), serviceId, status);
    }

    @Override
    public List<Account> listAccounts(UUID serviceUuid, AccountStatus status, String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);
        Long serviceId = null;
        if (serviceUuid != null) {
            serviceId = serviceRepositoryPort.findById(serviceUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceUuid))
                    .getId();
        }

        if (serviceId == null && status == null) {
            return accountRepositoryPort.findByVendorId(vendorId);
        }
        return accountRepositoryPort.findByVendorIdFiltered(vendorId, serviceId, status);
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
