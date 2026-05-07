package com.neversion.api.order.infrastructure.adapters.out;

import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;

@Repository
public class JpaOrderStatusHistoryAdapter implements OrderStatusHistoryPort {

    private final SpringDataOrderStatusChangeRepository repository;

    public JpaOrderStatusHistoryAdapter(SpringDataOrderStatusChangeRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderStatusChange record(OrderStatusChange change) {
        OrderStatusChangeEntity entity = OrderStatusChangeEntity.builder()
                .orderId(change.getOrderId())
                .oldStatus(change.getOldStatus())
                .newStatus(change.getNewStatus())
                .changedBy(change.getChangedBy())
                .notes(change.getNotes())
                .changedAt(change.getChangedAt() != null
                        ? change.getChangedAt().atOffset(ZoneOffset.UTC)
                        : java.time.OffsetDateTime.now())
                .build();

        OrderStatusChangeEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<OrderStatusChange> findByOrderId(Long orderId) {
        return repository.findByOrderIdOrderByChangedAtAsc(orderId).stream()
                .map(this::toDomain)
                .toList();
    }

    private OrderStatusChange toDomain(OrderStatusChangeEntity entity) {
        return OrderStatusChange.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .oldStatus(entity.getOldStatus())
                .newStatus(entity.getNewStatus())
                .changedBy(entity.getChangedBy())
                .notes(entity.getNotes())
                .changedAt(entity.getChangedAt() != null ? entity.getChangedAt().toInstant() : null)
                .build();
    }
}
