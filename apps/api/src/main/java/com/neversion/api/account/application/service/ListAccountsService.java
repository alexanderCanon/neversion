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
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-024: Returns the vendor's account list with optional filters.
 * Resolves vendorUuid → internal vendorId, then applies serviceUuid/status filters.
 */
@Service
public class ListAccountsService implements ListAccountsUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;

    public ListAccountsService(AccountRepositoryPort accountRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
    }

    @Override
    public List<Account> listByVendor(UUID vendorUuid, UUID serviceUuid, AccountStatus status) {
        Long vendorId = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid))
                .getId();

        // Resolve optional serviceUuid → internal serviceId
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
}
