package com.neversion.api.profile.infrastructure.adapters.out;

import java.time.LocalDateTime;
import java.util.UUID;

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
 * JPA entity for the 'profile_assignment_history' table (V32).
 *
 * Each row captures a snapshot of the master-account and profile-slot
 * credentials at the moment a subscription is activated. The row is closed
 * (releasedAt set) when the subscription is cancelled.
 */
@Entity
@Table(name = "profile_assignment_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAssignmentHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "account_email")
    private String accountEmail;

    @Column(name = "account_password")
    private String accountPassword;

    @Column(name = "profile_name", length = 100)
    private String profileName;

    @Column(name = "profile_pin", length = 20)
    private String profilePin;

    @Column(name = "profile_notes", columnDefinition = "TEXT")
    private String profileNotes;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    /** Null while assignment is active; set on revocation. */
    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "vendor_id")
    private Long vendorId;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }
}
