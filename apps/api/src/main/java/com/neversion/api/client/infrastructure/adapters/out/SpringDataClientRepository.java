package com.neversion.api.client.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByUuid(UUID uuid);

    Optional<ClientEntity> findByEmail(String email);

    Optional<ClientEntity> findByUserId(Long userId);

    List<ClientEntity> findByName(String name);

    List<ClientEntity> findByPhone(String phone);

    /**
     * US-029 — Lista clientes del vendor con filtros opcionales.
     * Si el parámetro es null se omite ese criterio (JPQL null-safe).
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.vendorId = :vendorId "
            + "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%')) "
            + "AND (:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) "
            + "ORDER BY c.createdAt DESC")
    List<ClientEntity> findByVendorId(
            @Param("vendorId") Long vendorId,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email);
}
