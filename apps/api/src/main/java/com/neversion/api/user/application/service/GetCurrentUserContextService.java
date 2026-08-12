package com.neversion.api.user.application.service;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.application.port.in.GetCurrentUserContextUseCase;
import com.neversion.api.user.domain.model.CurrentUserContextResult;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCurrentUserContextService implements GetCurrentUserContextUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public GetCurrentUserContextService(
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserContextResult get(String callerExternalId) {
        User user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));

        if (user.getRole() != UserRole.VENDOR) {
            return new CurrentUserContextResult(
                    user.getExternalId(),
                    user.getRole(),
                    null,
                    null);
        }

        Vendor vendor = vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor record not found for user: " + user.getExternalId()));

        return new CurrentUserContextResult(
                user.getExternalId(),
                user.getRole(),
                vendor.getUuid(),
                vendor.getStoreName());

    }
}

