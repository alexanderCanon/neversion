package com.neversion.api.account.infrastructure.adapters.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.ProfileDeliveryType;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.infrastructure.adapters.out.converter.ProfileDeliveryTypeConverter;
import com.neversion.api.account.infrastructure.adapters.out.converter.SaleModeConverter;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.domain.model.enums.AccountStatusConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
 * JPA Entity for the 'accounts' table.
 * Represents a master credential purchased from a wholesale provider
 * to operate a specific Service (e.g., a Netflix family account).
 *
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    /** Internal auto-increment PK — never exposed to the frontend. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** External UUID — used in all REST responses and frontend routes. */
    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /** FK to services.id — the platform this account belongs to. */
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    /** Provider master email credential. */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** Provider master password credential. */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * The date Neversion must pay the wholesaler to keep this account alive.
     */
    @Column(name = "renewal_date", nullable = false)
    private LocalDate renewalDate;

    /** Quality tier, e.g. "4K Ultra HD", "Familiar". */
    @Column(name = "plan", length = 100)
    private String plan;

    /**
     * Determines sales strategy: by individual profiles or as a full account.
     * Stored as lowercase varchar via SaleModeConverter (by_profile | full_account).
     */
    @Convert(converter = SaleModeConverter.class)
    @Column(name = "sale_mode", nullable = false, length = 20)
    private SaleMode saleMode;

    /** How access is delivered to the client for BY_PROFILE accounts. Null for FULL_ACCOUNT. */
    @Convert(converter = ProfileDeliveryTypeConverter.class)
    @Column(name = "profile_delivery_type", length = 30)
    private ProfileDeliveryType profileDeliveryType;

    /** Private admin notes about this account. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Acquisition cost paid to the wholesaler (US-006). */
    @Column(name = "cost", precision = 10, scale = 2)
    private java.math.BigDecimal cost;

    /** Where this account was purchased from (US-006). */
    @Column(name = "source", length = 255)
    private String source;

    /** Date the account was purchased from the wholesaler (US-006). */
    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    /** Operational status: available | partial | full | expired (US-006). */
    @Convert(converter = AccountStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.AVAILABLE;

    /** Maximum number of profiles this account can hold. Copied from service on creation. */
    @Column(name = "max_profiles", nullable = false)
    @Builder.Default
    private Integer maxProfiles = 1;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-006). DB FK by V12. */
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
