package com.neversion.api.reservation.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;

@SpringBootTest
@DisplayName("ReservationRepositoryPort integration tests")
class ReservationRepositoryIT extends BaseIntegrationTest {

        @Autowired
        private ReservationRepositoryPort reservationRepositoryPort;

        @Autowired
        private ClientRepositoryPort clientRepositoryPort;

        @Autowired
        private ServiceRepositoryPort serviceRepositoryPort;

        @Autowired
        private EntityManager entityManager;

        private Client parentClient;
        private Service parentService;

        @BeforeEach
        void setUp() {
                parentClient = clientRepositoryPort.save(
                                Client.builder()
                                                .name("Reservation Client")
                                                .phone("55500001111")
                                                .email("res-client-" + System.nanoTime() + "@test.com")
                                                .build());
                parentService = serviceRepositoryPort.save(
                                Service.builder()
                                                .name("Test Service " + System.nanoTime())
                                                .category(CategoryType.STREAMING)
                                                .maxProfiles(1)
                                                .build());
        }

        @AfterEach
        void tearDown() {
                // Clean up reservations that were not rolled back (e.g.,
                // expirePendingReservations test)
                List<Reservation> all = reservationRepositoryPort.findAll();
                for (Reservation r : all) {
                        // Details are cleaned up by cascade or we just leave them —
                        // Testcontainers container is shared but DB is cleaned between test classes
                }
        }

        private Reservation buildReservation(ReservationStatus status, Instant expirationDate) {
                return Reservation.builder()
                                .clientId(parentClient.getId())
                                .discount(BigDecimal.ZERO)
                                .total(new BigDecimal("100.00"))
                                .status(status)
                                .expirationDate(expirationDate)
                                .build();
        }

        @Test
        @Transactional
        @DisplayName("save - should persist reservation with pending status")
        void save_shouldPersistReservation_withPendingStatus() {
                // Given
                Reservation reservation = buildReservation(
                                ReservationStatus.PENDING,
                                Instant.now().plus(60, ChronoUnit.MINUTES));

                // When
                Reservation saved = reservationRepositoryPort.save(reservation);

                // Then
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
                assertThat(saved.getTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
                assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @Transactional
        @DisplayName("existsByReceiptUrl - should return true when url exists (BR-05)")
        void existsByReceiptUrl_shouldReturnTrue_whenUrlExists() {
                // Given
                Reservation reservation = buildReservation(
                                ReservationStatus.UPLOADED,
                                Instant.now().plus(60, ChronoUnit.MINUTES));
                reservation.setReceiptUrl("https://s3.example.com/receipt-001.jpg");
                reservationRepositoryPort.save(reservation);

                // When
                boolean exists = reservationRepositoryPort.existsByReceiptUrl("https://s3.example.com/receipt-001.jpg");

                // Then
                assertThat(exists).isTrue();
        }

        @Test
        @Transactional
        @DisplayName("existsByReceiptUrl - should return false when not exists")
        void existsByReceiptUrl_shouldReturnFalse_whenNotExists() {
                // When
                boolean exists = reservationRepositoryPort.existsByReceiptUrl("https://s3.example.com/nonexistent.jpg");

                // Then
                assertThat(exists).isFalse();
        }

        @Test
        @Transactional
        @DisplayName("findByStatus - should return matching reservations")
        void findByStatus_shouldReturnMatchingReservations() {
                // Given
                reservationRepositoryPort.save(buildReservation(
                                ReservationStatus.PENDING, Instant.now().plus(60, ChronoUnit.MINUTES)));
                reservationRepositoryPort.save(buildReservation(
                                ReservationStatus.PENDING, Instant.now().plus(60, ChronoUnit.MINUTES)));

                Reservation uploaded = buildReservation(
                                ReservationStatus.UPLOADED, Instant.now().plus(60, ChronoUnit.MINUTES));
                uploaded.setReceiptUrl("https://s3.example.com/receipt-filter-" + System.nanoTime() + ".jpg");
                reservationRepositoryPort.save(uploaded);

                // When
                List<Reservation> pendingList = reservationRepositoryPort.findByStatus(ReservationStatus.PENDING);

                // Then
                assertThat(pendingList).isNotEmpty();
                assertThat(pendingList).allMatch(r -> r.getStatus() == ReservationStatus.PENDING);
        }

        @Test
        @Transactional
        @DisplayName("findDetailsByReservationId - should return details")
        void findDetailsByReservationId_shouldReturnDetails() {
                // Given
                Reservation saved = reservationRepositoryPort.save(buildReservation(
                                ReservationStatus.PENDING, Instant.now().plus(60, ChronoUnit.MINUTES)));

                ReservationDetail detail = new ReservationDetail(
                                null,
                                null, // uuid generated on persist
                                saved.getId(),
                                parentService.getId(), // serviceId — FK satisfied
                                2,
                                new BigDecimal("50.00"),
                                null); // subtotal is GENERATED — do not set

                reservationRepositoryPort.saveDetail(detail);

                // Clear L1 cache so the next read fetches the GENERATED subtotal from DB
                entityManager.flush();
                entityManager.clear();

                // When
                List<ReservationDetail> details = reservationRepositoryPort.findDetailsByReservationId(saved.getId());

                // Then
                assertThat(details).hasSize(1);
                assertThat(details.get(0).serviceId()).isEqualTo(parentService.getId());
                assertThat(details.get(0).qty()).isEqualTo(2);
                assertThat(details.get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
                assertThat(details.get(0).subtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("expirePendingReservations - should transition pending to expired")
        void expirePendingReservations_shouldTransitionToExpired() {
                // Given — reservation with past expiration date
                Reservation expiredReservation = buildReservation(
                                ReservationStatus.PENDING,
                                Instant.now().minus(10, ChronoUnit.MINUTES));
                Reservation saved = reservationRepositoryPort.save(expiredReservation);

                // When — bulk update runs in its own transaction via @Modifying
                int affected = reservationRepositoryPort.expirePendingReservations();

                // Then
                assertThat(affected).isGreaterThanOrEqualTo(1);

                // Verify the status was changed — need a fresh read
                Optional<Reservation> reloaded = reservationRepositoryPort.findByUuid(saved.getUuid());
                assertThat(reloaded).isPresent();
                assertThat(reloaded.get().getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        }
}
