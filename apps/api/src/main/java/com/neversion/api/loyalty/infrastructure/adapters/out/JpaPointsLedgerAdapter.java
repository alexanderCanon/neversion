package com.neversion.api.loyalty.infrastructure.adapters.out;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;
import com.neversion.api.loyalty.infrastructure.adapters.out.mapper.PointsLedgerPersistenceMapper;

@Component
public class JpaPointsLedgerAdapter implements PointsLedgerRepositoryPort {

    private final SpringDataPointsLedgerRepository repository;
    private final PointsLedgerPersistenceMapper mapper;

    public JpaPointsLedgerAdapter(SpringDataPointsLedgerRepository repository,
            PointsLedgerPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PointsLedgerEntry save(PointsLedgerEntry entry) {
        PointsLedgerEntity saved = repository.save(mapper.toEntity(entry));
        return mapper.toDomain(saved);
    }

    @Override
    public long sumByClientIdAndStatus(Long clientId, PointsEntryStatus status) {
        return repository.sumPointsByClientIdAndStatus(clientId, status);
    }

    @Override
    public List<PointsLedgerEntry> findByClientId(Long clientId, Pageable pageable) {
        return repository.findByClientIdOrderByCreatedAtDesc(clientId, pageable)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByClientId(Long clientId) {
        return repository.countByClientId(clientId);
    }

    @Override
    public List<PointsLedgerEntry> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PointsLedgerEntry> findByReservationId(Long reservationId) {
        return repository.findByReservationId(reservationId).stream().map(mapper::toDomain).toList();
    }
}
