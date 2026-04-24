package com.neversion.api.order.infrastructure.adapters.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.neversion.api.order.domain.model.Order;
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
    public Optional<Order> findByReservationId(UUID reservationId) {
        return orderRepo.findByReservationId(reservationId).map(orderMapper::toDomain);
    }
}
