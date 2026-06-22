package com.neversion.api.client.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for an end consumer (client) of Neversion's services
 *
 * 'id' (Long) – internal identifier, used only for DB relations. Never exposed
 * externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and
 * frontend routes.
 * 'phone' is the primary contact channel used for WhatsApp payment
 * reminders.
 */
@Getter
@Setter
@Builder
public class Client {

    /** Internal DB PK — used for JPA relations (subscriptions FK). */
    private Long id;

    /** External identifier — exposed to the frontend instead of the numeric id. */
    private UUID uuid;

    /** FK to users.id — the authenticated identity behind this client (US-003). */
    private Long userId;

    /** FK to vendors.id — multi-tenancy isolation (ADR-02, US-003). */
    private Long vendorId;

    private String name;

    /** Primary contact channel — used for WhatsApp automation reminders. */
    private String phone;

    private String email;

    /**
     * Private admin notes about this client (e.g. payment history, preferences).
     */
    private String notes;

    private LocalDateTime createdAt;

    public Client() {
    }

    public Client(Long id, UUID uuid, Long userId, Long vendorId, String name, String phone, String email,
            String notes, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.vendorId = vendorId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
