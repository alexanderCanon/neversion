package com.neversion.api.order.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.infrastructure.adapters.out.mapper.OrderPersistenceMapper;

/**
 * JPA adapter — US-008: uses Long PK internally, findByUuid externally.
 */
@Repository
public class JpaOrderAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository orderRepo;
    private final OrderPersistenceMapper orderMapper;

    public JpaOrderAdapter(SpringDataOrderRepository orderRepo,
            OrderPersistenceMapper orderMapper) {
        this.orderRepo = orderRepo;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity saved = orderRepo.saveAndFlush(entity);
        return orderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findByUuid(UUID uuid) {
        return orderRepo.findByUuid(uuid).map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByInternalId(Long id) {
        return orderRepo.findById(id).map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByReservationId(Long reservationId) {
        return orderRepo.findByReservationId(reservationId).map(orderMapper::toDomain);
    }

    /** US-030 — Historial de órdenes del cliente (JOIN via reservations). */
    @Override
    public List<Order> findByClientId(Long clientId) {
        return orderRepo.findByClientId(clientId).stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByVendorIdFiltered(Long vendorId, Long clientId, OrderStatus status) {
        Specification<OrderEntity> specification = byVendorId(vendorId)
                .and(byClientId(clientId))
                .and(byStatus(status));

        return orderRepo.findAll(specification, Sort.by(Sort.Direction.ASC, "createdAt")).stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    private Specification<OrderEntity> byVendorId(Long vendorId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("vendorId"), vendorId);
    }

    private Specification<OrderEntity> byClientId(Long clientId) {
        return (root, query, criteriaBuilder) -> clientId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("clientId"), clientId);
    }

    private Specification<OrderEntity> byStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }
}
