package com.neversion.api.client.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.client.domain.model.Client;

/**
 * Inbound port (use-case interface) for client management (EPIC-04).
 *
 * Naming:
 *   create()         — legacy / store registration (no JWT context).
 *   createForVendor() — US-031: manual creation from panel (JWT owner).
 *   listByVendor()   — US-029: filtered list scoped to the caller's tenant.
 *   getDetail()      — US-030: full detail with subscriptions + orders.
 *   update()         — US-032: edit basic fields (name, phone, notes only).
 */
public interface ClientUseCase {

    // ── Legacy (kept for backward compat with auth flow) ─────────────────
    Client create(Client client);

    // ── US-029 — Listar clientes del vendor ────────────────────────────
    /**
     * Returns all clients belonging to the vendor identified by {@code vendorUuid}.
     * Ownership check: callerExternalId must map to the same vendor.
     * Filters name, phone, email are optional (null = no filter).
     */
    List<Client> listByVendor(UUID vendorUuid, String name, String phone, String email,
            String callerExternalId);

    // ── US-030 — Detalle de cliente ────────────────────────────────────
    /**
     * Returns full client data. Ownership check: 403 if caller does not own the client.
     * Subscriptions and order history are loaded by the service layer.
     */
    ClientDetail getDetail(UUID clientUuid, String callerExternalId);

    // ── US-031 — Crear cliente manual ──────────────────────────────────
    /**
     * Creates a client record linked to the caller's vendor.
     * Validates email uniqueness (400 if already exists).
     * Logs a CLIENT_WELCOME entry in notification_log.
     */
    Client createForVendor(Client client, String callerExternalId);

    // ── US-032 — Editar datos básicos ──────────────────────────────────
    /**
     * Updates name, phone, notes only. email is immutable (BR-US032-01).
     * Ownership check: 403 if caller does not own the client.
     */
    Client update(UUID clientUuid, String name, String phone, String notes,
            String callerExternalId);

    /**
     * US-041: Returns full access credentials (active subscriptions) for the authenticated client.
     * The client is resolved from the caller's JWT.
     */
    List<ClientAccessDetail> getMyAccesses(String callerExternalId);

    // ── Generic getters (legacy) ───────────────────────────────────────
    Client getById(UUID uuid);
    List<Client> getByName(String name);
    List<Client> getByPhone(String phone);
    List<Client> getAll();
    void delete(UUID uuid);

    // ── Inner record for US-030 detail ────────────────────────────────
    record ClientDetail(
            Client client,
            List<ActiveSubscriptionSummary> activeSubscriptions,
            List<OrderSummary> orderHistory) {}

    record ActiveSubscriptionSummary(
            UUID id,
            String serviceName,
            String profileName,
            java.time.LocalDate paymentDueDate,
            String status) {}

    record OrderSummary(
            UUID id,
            String status,
            java.time.Instant createdAt) {}

    record ClientAccessDetail(
            UUID subscriptionId,
            String serviceName,
            String accountEmail,
            String accountPassword,
            String profileName,
            String profilePin,
            java.time.LocalDate paymentDueDate,
            String status) {}
}
