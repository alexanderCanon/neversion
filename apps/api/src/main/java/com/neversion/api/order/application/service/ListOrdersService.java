package com.neversion.api.order.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.application.port.in.ListOrdersUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * UC5: List Orders — US-037.
 */
@Service
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public ListOrdersService(
            OrderRepositoryPort orderRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> listByVendor(UUID vendorUuid, UUID clientUuid, OrderStatus status, String callerExternalId) {

        // 1. Resolve internal vendor ID
        Vendor targetVendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorUuid));

        // 2. Ownership check (ADR-02)
        User caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));
        
        Vendor callerVendor = vendorRepositoryPort.findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for caller"));

        if (!targetVendor.getId().equals(callerVendor.getId())) {
            throw new AccessDeniedException("You do not have permission to view orders from this vendor.");
        }

        // 3. Resolve internal client ID (if provided)
        Long clientId = null;
        if (clientUuid != null) {
            clientId = clientRepositoryPort.findById(clientUuid)
                    .map(Client::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientUuid));
        }

        // 4. Query filtered orders
        return orderRepositoryPort.findByVendorIdFiltered(targetVendor.getId(), clientId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> listOrders(UUID clientUuid, OrderStatus status, String callerExternalId) {
        User caller = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));
        Vendor callerVendor = vendorRepositoryPort.findByUserId(caller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for caller"));

        Long clientId = null;
        if (clientUuid != null) {
            clientId = clientRepositoryPort.findById(clientUuid)
                    .map(Client::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientUuid));
        }

        return orderRepositoryPort.findByVendorIdFiltered(callerVendor.getId(), clientId, status);
    }
}

