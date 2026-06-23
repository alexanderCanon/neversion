package com.neversion.api.order.infrastructure.adapters.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.model.enums.UserRole;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@SpringBootTest
@Transactional
@DisplayName("OrderRepositoryPort and OrderStatusHistoryPort Integration Tests")
class OrderRepositoryIT extends BaseIntegrationTest {

    @Autowired private OrderRepositoryPort orderRepositoryPort;
    @Autowired private OrderStatusHistoryPort orderStatusHistoryPort;
    @Autowired private UserRepositoryPort userRepositoryPort;
    @Autowired private VendorRepositoryPort vendorRepositoryPort;
    @Autowired private ClientRepositoryPort clientRepositoryPort;
    @Autowired private ReservationRepositoryPort reservationRepositoryPort;

    private Vendor parentVendor;
    private Client parentClient;

    @BeforeEach
    void setUp() {
        User user = userRepositoryPort.save(User.builder()
                .externalId("auth|order-it-" + System.nanoTime())
                .role(UserRole.VENDOR)
                .build());

        parentVendor = vendorRepositoryPort.save(Vendor.builder()
                .userId(user.getId())
                .storeName("Order IT Vendor Store")
                .build());

        parentClient = clientRepositoryPort.save(Client.builder()
                .name("Order IT Client")
                .email("order-it-client-" + System.nanoTime() + "@example.com")
                .phone("55500000000")
                .build());
    }

    @Test
    @DisplayName("save - should persist order and generate UUID and retrieve by UUID / Internal ID")
    void save_shouldPersistOrder_andRetrieveSuccessfully() {
        Order order = Order.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .status(OrderStatus.PENDING)
                .paymentMethod("TRANSFERENCIA")
                .receiptUrl("https://receipts.com/order-it.png")
                .total(new BigDecimal("100.00"))
                .discount(new BigDecimal("5.00"))
                .notes("Order test notes")
                .build();

        Order saved = orderRepositoryPort.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Find by UUID
        Optional<Order> byUuid = orderRepositoryPort.findByUuid(saved.getUuid());
        assertThat(byUuid).isPresent();
        assertThat(byUuid.get().getId()).isEqualTo(saved.getId());
        assertThat(byUuid.get().getReceiptUrl()).isEqualTo("https://receipts.com/order-it.png");
        assertThat(byUuid.get().getTotal()).isEqualByComparingTo("100.00");

        // Find by Internal ID
        Optional<Order> byInternalId = orderRepositoryPort.findByInternalId(saved.getId());
        assertThat(byInternalId).isPresent();
        assertThat(byInternalId.get().getUuid()).isEqualTo(saved.getUuid());
    }

    @Test
    @DisplayName("findByReservationId - should return order matching the reservation ID")
    void findByReservationId_shouldReturnOrder() {
        Reservation reservation = reservationRepositoryPort.save(Reservation.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .status(ReservationStatus.PENDING)
                .total(new BigDecimal("100.00"))
                .discount(BigDecimal.ZERO)
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        Order order = orderRepositoryPort.save(Order.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .reservationId(reservation.getId())
                .reservationUuid(reservation.getUuid())
                .status(OrderStatus.VALIDATED)
                .build());

        Optional<Order> result = orderRepositoryPort.findByReservationId(reservation.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(order.getId());
        assertThat(result.get().getReservationId()).isEqualTo(reservation.getId());
    }

    @Test
    @DisplayName("findByClientId - should return client orders ordered by createdAt DESC (native query JOIN)")
    void findByClientId_shouldReturnOrdersOrderedByCreatedAtDesc() throws Exception {
        // We need reservations because findByClientId joins reservations on reservation_id
        Reservation res1 = reservationRepositoryPort.save(Reservation.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .status(ReservationStatus.VALIDATED)
                .total(new BigDecimal("100.00"))
                .discount(BigDecimal.ZERO)
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        // Sleep briefly to ensure distinct timestamps if DB supports microsecond precision
        Thread.sleep(10);

        Reservation res2 = reservationRepositoryPort.save(Reservation.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .status(ReservationStatus.VALIDATED)
                .total(new BigDecimal("150.00"))
                .discount(BigDecimal.ZERO)
                .expirationDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        Order order1 = orderRepositoryPort.save(Order.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .reservationId(res1.getId())
                .reservationUuid(res1.getUuid())
                .status(OrderStatus.VALIDATED)
                .build());

        Thread.sleep(10);

        Order order2 = orderRepositoryPort.save(Order.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .reservationId(res2.getId())
                .reservationUuid(res2.getUuid())
                .status(OrderStatus.VALIDATED)
                .build());

        List<Order> clientOrders = orderRepositoryPort.findByClientId(parentClient.getId());

        assertThat(clientOrders).hasSize(2);
        // Order 2 should be first since it was created later and ordered by created_at DESC
        assertThat(clientOrders.get(0).getId()).isEqualTo(order2.getId());
        assertThat(clientOrders.get(1).getId()).isEqualTo(order1.getId());
    }

    @Test
    @DisplayName("findByVendorIdFiltered - should return vendor orders filtering by client and status")
    void findByVendorIdFiltered_shouldApplySpecificationFilters() {
        Client anotherClient = clientRepositoryPort.save(Client.builder()
                .name("Another Client")
                .email("another-" + System.nanoTime() + "@example.com")
                .phone("55500000001")
                .build());

        Order order1 = orderRepositoryPort.save(Order.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .status(OrderStatus.PENDING)
                .build());

        Order order2 = orderRepositoryPort.save(Order.builder()
                .clientId(anotherClient.getId())
                .vendorId(parentVendor.getId())
                .status(OrderStatus.COMPLETED)
                .build());

        // Order for a different vendor
        User anotherUser = userRepositoryPort.save(User.builder()
                .externalId("auth|another-vendor-" + System.nanoTime())
                .role(UserRole.VENDOR)
                .build());
        Vendor anotherVendor = vendorRepositoryPort.save(Vendor.builder()
                .userId(anotherUser.getId())
                .storeName("Another Vendor Store")
                .build());

        Order order3 = orderRepositoryPort.save(Order.builder()
                .clientId(parentClient.getId())
                .vendorId(anotherVendor.getId())
                .status(OrderStatus.PENDING)
                .build());

        // 1. Filter by vendorId only
        List<Order> byVendor = orderRepositoryPort.findByVendorIdFiltered(parentVendor.getId(), null, null);
        assertThat(byVendor).hasSize(2);
        assertThat(byVendor).extracting(Order::getId).containsExactlyInAnyOrder(order1.getId(), order2.getId());

        // 2. Filter by vendorId and clientId
        List<Order> byVendorAndClient = orderRepositoryPort.findByVendorIdFiltered(parentVendor.getId(), parentClient.getId(), null);
        assertThat(byVendorAndClient).hasSize(1);
        assertThat(byVendorAndClient.get(0).getId()).isEqualTo(order1.getId());

        // 3. Filter by vendorId and status
        List<Order> byVendorAndStatus = orderRepositoryPort.findByVendorIdFiltered(parentVendor.getId(), null, OrderStatus.COMPLETED);
        assertThat(byVendorAndStatus).hasSize(1);
        assertThat(byVendorAndStatus.get(0).getId()).isEqualTo(order2.getId());

        // 4. Filter by vendorId, clientId and status mismatch
        List<Order> noMatch = orderRepositoryPort.findByVendorIdFiltered(parentVendor.getId(), parentClient.getId(), OrderStatus.COMPLETED);
        assertThat(noMatch).isEmpty();
    }

    @Test
    @DisplayName("record and find status changes history")
    void record_and_findHistory() throws Exception {
        Order order = orderRepositoryPort.save(Order.builder()
                .clientId(parentClient.getId())
                .vendorId(parentVendor.getId())
                .status(OrderStatus.PENDING)
                .build());

        OrderStatusChange change1 = OrderStatusChange.builder()
                .orderId(order.getId())
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.VALIDATED)
                .changedBy("vendor-caller")
                .notes("Receipt validated")
                .changedAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .build();

        OrderStatusChange change2 = OrderStatusChange.builder()
                .orderId(order.getId())
                .oldStatus(OrderStatus.VALIDATED)
                .newStatus(OrderStatus.COMPLETED)
                .changedBy("system")
                .notes("Assignment completed")
                .changedAt(Instant.now())
                .build();

        orderStatusHistoryPort.record(change1);
        orderStatusHistoryPort.record(change2);

        List<OrderStatusChange> history = orderStatusHistoryPort.findByOrderId(order.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getOldStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(history.get(0).getNewStatus()).isEqualTo(OrderStatus.VALIDATED);
        assertThat(history.get(0).getNotes()).isEqualTo("Receipt validated");

        assertThat(history.get(1).getOldStatus()).isEqualTo(OrderStatus.VALIDATED);
        assertThat(history.get(1).getNewStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
