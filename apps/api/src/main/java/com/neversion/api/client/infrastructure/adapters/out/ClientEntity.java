package com.neversion.api.client.infrastructure.adapters.out;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for the 'clients' table.
 * Represents the end consumer utilizing and paying for streaming profiles.
 * Formerly "UserGuestEntity" — renamed to align with unified domain language.
 *
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 * 'phone' is the primary contact channel for WhatsApp automation notifications.
 */
@Entity
@Table(name = "clients")
@SQLDelete(sql = "UPDATE clients SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

    /** Internal auto-increment PK — never exposed to the frontend. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** External UUID — used in all REST responses and frontend routes. */
    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /** FK to users.id — authenticated identity (US-003). DB FK enforced by V9. */
    @Column(name = "user_id")
    private Long userId;

    /** FK to vendors.id — multi-tenancy isolation (ADR-02, US-003). DB FK enforced by V9. */
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Primary channel for WhatsApp payment reminders and automations. */
    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    /** Private admin notes about this client. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Soft delete timestamp — null means the record is active. */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
