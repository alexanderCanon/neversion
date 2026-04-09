package com.neversion.api.subscription.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.enums.SubStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for an active subscription.
 * Represents the binding link between a Client and a specific Profile,
 * along with their payment timeline (start_date → payment_due_date).
 *
 * 'id' (Long)  – internal identifier, used only for DB relations. Never exposed externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and frontend routes.
 * 'paymentDueDate' is the critical field polled by n8n at 7-day, 3-day and overdue intervals.
 *
 * UUID transient fields (profileUuid, clientUuid, accountUuid) are populated from the REST
 * request and resolved to Long IDs inside the application service before persistence.
 */
@Getter
@Setter
@Builder
public class Subscription {

    /** Internal DB PK — used for JPA relations. */
    private Long id;

    /** External identifier — exposed to the frontend instead of the numeric id. */
    private UUID uuid;

    // ── Internal FK IDs (resolved before persistence) ───────────────────────

    /** FK to Client (Long) — resolved from clientUuid in service layer. */
    private Long clientId;

    /** FK to Profile (Long) — resolved from profileUuid in service layer. */
    private Long profileId;

    // ── Transient UUID fields — sent by the REST layer, resolved in service ──

    /** Incoming UUID from the REST request for the target Profile. */
    private UUID profileUuid;

    /** Incoming UUID from the REST request for the Client. */
    private UUID clientUuid;

    /** Incoming UUID from the REST request for the Account (for context/display). */
    private UUID accountUuid;

    // ── Business fields ──────────────────────────────────────────────────────

    /** Date the client's access lifecycle began. Defaults to today. */
    private LocalDate startDate;

    /**
     * The date by which the client must pay to retain access.
     * Automations (n8n) use this field to trigger reminder sequences.
     */
    private LocalDate paymentDueDate;

    /** Number of months the client has paid. Incremented on each renewal. */
    private Long monthsPaid;

    /**
     * Current access status.
     * ACTIVE    – client has valid access.
     * SUSPENDED – missed payment window; access cut, reactivation possible.
     * CANCELLED – permanent termination (BR-11).
     */
    private SubStatus status;

    /** Admin notes for this subscription (e.g. "has 35 credit"). */
    private String notes;

    private LocalDateTime createdAt;

    public Subscription() {
    }

    public Subscription(Long id, UUID uuid, Long clientId, Long profileId,
            UUID profileUuid, UUID clientUuid, UUID accountUuid,
            LocalDate startDate, LocalDate paymentDueDate,
            Long monthsPaid, SubStatus status, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.clientId = clientId;
        this.profileId = profileId;
        this.profileUuid = profileUuid;
        this.clientUuid = clientUuid;
        this.accountUuid = accountUuid;
        this.startDate = startDate;
        this.paymentDueDate = paymentDueDate;
        this.monthsPaid = monthsPaid;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
