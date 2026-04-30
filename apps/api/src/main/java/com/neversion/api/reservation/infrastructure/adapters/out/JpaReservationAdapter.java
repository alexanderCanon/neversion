package com.neversion.api.reservation.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.reservation.infrastructure.adapters.out.mapper.ReservationPersistenceMapper;

/**
 * US-009: Long PK internally, findByUuid externally.
 */
@Repository
public class JpaReservationAdapter implements ReservationRepositoryPort {

    private final SpringDataReservationRepository reservationRepository;
    private final SpringDataReservationDetailRepository detailRepository;
    private final ReservationPersistenceMapper mapper;

    public JpaReservationAdapter(
            SpringDataReservationRepository reservationRepository,
            SpringDataReservationDetailRepository detailRepository,
            ReservationPersistenceMapper mapper) {
        this.reservationRepository = reservationRepository;
        this.detailRepository = detailRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByReceiptUrl(String receiptUrl) {
        return reservationRepository.existsByReceiptUrl(receiptUrl);
    }

    @Override
    public boolean existsActiveRenewalBySubscriptionId(Long subscriptionId) {
        return reservationRepository.existsActiveRenewalBySubscriptionId(subscriptionId);
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = mapper.toEntity(reservation);
        ReservationEntity saved = reservationRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Reservation update(Reservation reservation) {
        ReservationEntity entity = mapper.toEntity(reservation);
        reservationRepository.findById(entity.getId())
                .ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        ReservationEntity saved = reservationRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Reservation> findByUuid(UUID uuid) {
        return reservationRepository.findByUuid(uuid)
                .map(entity -> {
                    Reservation reservation = mapper.toDomain(entity);
                    List<ReservationDetail> details = findDetailsByReservationId(entity.getId());
                    reservation.setDetails(details);
                    return reservation;
                });
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id)
                .map(entity -> {
                    Reservation reservation = mapper.toDomain(entity);
                    List<ReservationDetail> details = findDetailsByReservationId(entity.getId());
                    reservation.setDetails(details);
                    return reservation;
                });
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        return reservationRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(entity -> {
                    Reservation reservation = mapper.toDomain(entity);
                    reservation.setDetails(findDetailsByReservationId(entity.getId()));
                    return reservation;
                })
                .toList();
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public int expirePendingReservations() {
        return reservationRepository.expirePendingReservations();
    }

    @Override
    public ReservationDetail saveDetail(ReservationDetail detail) {
        ReservationDetailEntity entity = mapper.toEntity(detail);
        ReservationDetailEntity saved = detailRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<ReservationDetail> findDetailsByReservationId(Long reservationId) {
        return detailRepository.findByReservationId(reservationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
