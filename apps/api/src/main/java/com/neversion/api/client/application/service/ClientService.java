package com.neversion.api.client.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.client.application.port.in.ClientUseCase;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * Application service for EPIC-04 — Client management.
 *
 * Conventions:
 *   - resolveVendorId() — resolves caller's vendorId from JWT externalId (ADR-09).
 *   - Ownership: callerVendorId must equal client.vendorId, else AccessDeniedException (403).
 *   - email is immutable after creation (BR-US032-01).
 *   - Notification log registered on CLIENT_WELCOME (US-031, EPIC-08 will send actual email).
 */
@Service
public class ClientService implements ClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;

    public ClientService(
            ClientRepositoryPort clientRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            NotificationLogPort notificationLogPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
    }

    // ── Legacy create (auth flow, no JWT context) ──────────────────────────

    @Override
    @Transactional
    public Client create(Client client) {
        return clientRepositoryPort.save(client);
    }

    // ── US-029 — Listar clientes del vendor ────────────────────────────────

    /**
     * Returns clients scoped to the caller's vendor with optional filters.
     * Ownership verified: callerExternalId must map to vendorUuid (BR-US029-01).
     */
    @Override
    public List<Client> listByVendor(UUID vendorUuid, String name, String phone, String email,
            String callerExternalId) {
        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));

        // Ownership check — BR-US029-01
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(vendor.getId())) {
            throw new AccessDeniedException("Access denied: you do not own vendor " + vendorUuid);
        }

        return clientRepositoryPort.findByVendorId(vendor.getId(), name, phone, email);
    }

    // ── US-030 — Detalle de cliente ────────────────────────────────────────

    /**
     * Returns full client data + active subscriptions + order history.
     * Ownership check: 403 if caller's vendorId != client.vendorId (BR-US030-02).
     */
    @Override
    public ClientDetail getDetail(UUID clientUuid, String callerExternalId) {
        Client client = clientRepositoryPort.findById(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientUuid));

        // Ownership check — BR-US030-02
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(client.getVendorId())) {
            throw new AccessDeniedException("Access denied: client " + clientUuid
                    + " does not belong to your vendor");
        }

        // Active subscriptions — BR-US030-01
        List<Subscription> subs = subscriptionRepositoryPort.findByClientId(client.getId());
        List<ActiveSubscriptionSummary> activeSubs = subs.stream()
                .filter(s -> SubStatus.ACTIVE.equals(s.getStatus()))
                .map(s -> {
                    Profile p = profileRepositoryPort.findByInternalId(s.getProfileId()).orElse(null);
                    Account a = p != null ? accountRepositoryPort.findByInternalId(p.getAccountId()).orElse(null) : null;
                    com.neversion.api.service.domain.model.Service svc = a != null ? serviceRepositoryPort.findByInternalId(a.getServiceId()).orElse(null) : null;

                    return new ActiveSubscriptionSummary(
                            s.getUuid(),
                            svc != null ? svc.getName() : "Unknown Service",
                            p != null ? p.getName() : "Unknown Profile",
                            s.getPaymentDueDate(),
                            s.getStatus() != null ? s.getStatus().name() : null);
                })
                .toList();

        // Order history — BR-US030-01
        List<Order> orders = orderRepositoryPort.findByClientId(client.getId());
        List<OrderSummary> orderHistory = orders.stream()
                .map(o -> new OrderSummary(
                        o.getUuid(),
                        o.getStatus() != null ? o.getStatus().name() : null,
                        o.getCreatedAt()))
                .toList();

        return new ClientDetail(client, activeSubs, orderHistory);
    }

    // ── US-031 — Crear cliente manual ──────────────────────────────────────

    /**
     * Creates a client linked to the caller's vendor.
     * Validates email uniqueness: 400 if email already exists (BR-US031-03).
     * Registers CLIENT_WELCOME in notification_log for EPIC-08.
     */
    @Override
    @Transactional
    public Client createForVendor(Client client, String callerExternalId) {
        // BR-US031-03 — email uniqueness
        if (client.getEmail() != null && !client.getEmail().isBlank()) {
            clientRepositoryPort.findByEmail(client.getEmail()).ifPresent(existing -> {
                throw new IllegalArgumentException(
                        "Email already registered: " + client.getEmail());
            });
        }

        // Resolve vendor from JWT — BR-US031-01
        Long vendorId = resolveVendorId(callerExternalId);
        Client toSave = Client.builder()
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .notes(client.getNotes())
                .vendorId(vendorId)
                .build();

        Client saved = clientRepositoryPort.save(toSave);

        // Notification log — EPIC-08 will send the actual email
        String payload = String.format("{\"clientId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\"}",
                saved.getUuid(), saved.getName(), saved.getEmail());
        notificationLogPort.record("CLIENT_WELCOME", saved.getEmail(), payload,
                "client", saved.getId(), "welcome");

        return saved;
    }

    // ── US-032 — Editar datos básicos ──────────────────────────────────────

    /**
     * Updates name, phone, notes only.
     * email is immutable (BR-US032-01).
     * Ownership check: 403 if caller's vendorId != client.vendorId (BR-US032-02).
     */
    @Override
    @Transactional
    public Client update(UUID clientUuid, String name, String phone, String notes,
            String callerExternalId) {
        Client existing = clientRepositoryPort.findById(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientUuid));

        // Ownership check — BR-US032-02
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(existing.getVendorId())) {
            throw new AccessDeniedException("Access denied: client " + clientUuid
                    + " does not belong to your vendor");
        }

        // BR-US032-01 — email is NOT updated
        existing.setName(name);
        existing.setPhone(phone);
        existing.setNotes(notes);

        return clientRepositoryPort.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientAccessDetail> getMyAccesses(String callerExternalId) {
        // Strict ownership: resolve the client ID directly from the authenticated user
        Long clientId = resolveClientId(callerExternalId);

        List<Subscription> subs = subscriptionRepositoryPort.findByClientId(clientId);
        return subs.stream()
                .filter(s -> SubStatus.ACTIVE.equals(s.getStatus())
                        || SubStatus.SUSPENDED.equals(s.getStatus()))
                .map(s -> {
                    Profile p = profileRepositoryPort.findByInternalId(s.getProfileId())
                            .orElseThrow(() -> new ResourceNotFoundException("Profile not found for sub: " + s.getUuid()));
                    Account a = accountRepositoryPort.findByInternalId(p.getAccountId())
                            .orElseThrow(() -> new ResourceNotFoundException("Account not found for profile: " + p.getUuid()));
                    com.neversion.api.service.domain.model.Service svc = serviceRepositoryPort.findByInternalId(a.getServiceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Service not found for account: " + a.getUuid()));

                    return new ClientAccessDetail(
                            s.getUuid(),
                            svc.getName(),
                            SubStatus.ACTIVE.equals(s.getStatus()) ? a.getEmail() : null,
                            SubStatus.ACTIVE.equals(s.getStatus()) ? a.getPassword() : null,
                            p.getName(),
                            SubStatus.ACTIVE.equals(s.getStatus()) ? p.getPin() : null,
                            s.getPaymentDueDate(),
                            s.getStatus().name());
                })
                .toList();
    }

    // ── Generic getters (legacy, kept for backward compat) ────────────────

    @Override
    public Client getById(UUID uuid) {
        return clientRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + uuid));
    }

    @Override
    public List<Client> getByName(String name) {
        return clientRepositoryPort.findByName(name);
    }

    @Override
    public List<Client> getByPhone(String phone) {
        return clientRepositoryPort.findByPhone(phone);
    }

    @Override
    public List<Client> getAll() {
        return clientRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        clientRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + uuid));
        clientRepositoryPort.deleteById(uuid);
    }

    // ── Helper — ownership resolution ─────────────────────────────────────

    /**
     * Resolves the internal clientId from the caller's Supabase externalId.
     */
    private Long resolveClientId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return clientRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client record not found for userId: " + user.getId()))
                .getId();
    }

    /**
     * Resolves the internal vendorId from the caller's Supabase externalId (ADR-09).
     */
    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for userId: " + user.getId()))
                .getId();
    }
}
