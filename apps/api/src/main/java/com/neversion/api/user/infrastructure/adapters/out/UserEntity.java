package com.neversion.api.user.infrastructure.adapters.out;

import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.infrastructure.adapters.out.converter.UserRoleConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * JPA entity for the users table.
 * Infrastructure concern only — never crosses the domain boundary.
 */
@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External auth provider subject claim (ADR-06 / ADR-09). */
    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private String externalId;

    /** Platform role — persisted as lowercase string (NFR-06). */
    @Convert(converter = UserRoleConverter.class)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

