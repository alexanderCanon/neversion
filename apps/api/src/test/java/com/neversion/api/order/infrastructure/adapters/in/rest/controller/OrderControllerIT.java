package com.neversion.api.order.infrastructure.adapters.in.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.neversion.api.BaseIntegrationTest;
import com.neversion.api.order.application.port.in.ChangeOrderStatusUseCase;
import com.neversion.api.order.application.port.in.GetOrderUseCase;
import com.neversion.api.order.application.port.in.ListOrdersUseCase;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

import com.neversion.api.BaseWebIntegrationTest;

@DisplayName("OrderController and OrderGetController Web Integration Tests")
class OrderControllerIT extends BaseWebIntegrationTest {


    private static final String JWT_SECRET =
            "test-secret-key-for-testing-purposes-only-min-256-bits!!";

    private String buildJwt(String role, String subject) throws Exception {
        JWSSigner signer = new MACSigner(JWT_SECRET.getBytes());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .claim("app_metadata", Map.of("role", role))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    @Nested
    @DisplayName("PUT /api/v1/orders/{id}/complete")
    class CompleteOrderTests {

        @Test
        @DisplayName("should complete order successfully for vendor owner")
        void completeOrder_success() throws Exception {
            UUID orderUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            
            Order completedOrder = Order.builder()
                    .uuid(orderUuid)
                    .status(OrderStatus.COMPLETED)
                    .total(new BigDecimal("100.00"))
                    .build();

            when(changeOrderStatusUseCase.changeStatus(eq(orderUuid), eq(OrderStatus.COMPLETED), eq("completed notes"), eq(callerSubject)))
                    .thenReturn(completedOrder);

            mockMvc.perform(put("/api/v1/orders/" + orderUuid + "/complete")
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"notes\":\"completed notes\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("should return 401 when no token is provided")
        void completeOrder_unauthorized() throws Exception {
            mockMvc.perform(put("/api/v1/orders/" + UUID.randomUUID() + "/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/orders/{id}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("should cancel order successfully for vendor owner")
        void cancelOrder_success() throws Exception {
            UUID orderUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            
            Order cancelledOrder = Order.builder()
                    .uuid(orderUuid)
                    .status(OrderStatus.CANCELLED)
                    .total(new BigDecimal("100.00"))
                    .build();

            when(changeOrderStatusUseCase.changeStatus(eq(orderUuid), eq(OrderStatus.CANCELLED), eq("cancelled notes"), eq(callerSubject)))
                    .thenReturn(cancelledOrder);

            mockMvc.perform(put("/api/v1/orders/" + orderUuid + "/cancel")
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"notes\":\"cancelled notes\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{id}")
    class GetOrderDetailTests {

        @Test
        @DisplayName("should return full order detail when caller owns the order")
        void getOrderDetail_success() throws Exception {
            UUID orderUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            Long vendorId = 5L;
            Long reservationId = 20L;

            Order order = Order.builder()
                    .id(10L)
                    .uuid(orderUuid)
                    .vendorId(vendorId)
                    .reservationId(reservationId)
                    .status(OrderStatus.VALIDATED)
                    .total(new BigDecimal("150.00"))
                    .build();

            User user = User.builder().id(1L).externalId(callerSubject).build();
            Vendor vendor = Vendor.builder().id(vendorId).userId(1L).build();
            Reservation reservation = Reservation.builder().id(reservationId).uuid(UUID.randomUUID()).build();
            OrderStatusChange change = OrderStatusChange.builder()
                    .oldStatus(OrderStatus.PENDING)
                    .newStatus(OrderStatus.VALIDATED)
                    .changedBy("vendor")
                    .notes("Good")
                    .build();

            when(getOrderUseCase.getByUuid(orderUuid)).thenReturn(Optional.of(order));
            when(userRepositoryPort.findByExternalId(callerSubject)).thenReturn(Optional.of(user));
            when(vendorRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(vendor));
            when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
            when(orderStatusHistoryPort.findByOrderId(10L)).thenReturn(List.of(change));

            mockMvc.perform(get("/api/v1/orders/" + orderUuid)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderUuid.toString()))
                    .andExpect(jsonPath("$.status").value("VALIDATED"))
                    .andExpect(jsonPath("$.total").value(150.00))
                    .andExpect(jsonPath("$.statusHistory[0].newStatus").value("VALIDATED"));
        }

        @Test
        @DisplayName("should return 403 when caller is not the owner vendor")
        void getOrderDetail_forbidden() throws Exception {
            UUID orderUuid = UUID.randomUUID();
            String callerSubject = "auth|other-vendor";
            Long vendorId = 5L; // Order belongs to vendor 5

            Order order = Order.builder()
                    .id(10L)
                    .uuid(orderUuid)
                    .vendorId(vendorId)
                    .build();

            User user = User.builder().id(2L).externalId(callerSubject).build();
            Vendor callerVendor = Vendor.builder().id(6L).userId(2L).build(); // Caller vendor ID is 6

            when(getOrderUseCase.getByUuid(orderUuid)).thenReturn(Optional.of(order));
            when(userRepositoryPort.findByExternalId(callerSubject)).thenReturn(Optional.of(user));
            when(vendorRepositoryPort.findByUserId(2L)).thenReturn(Optional.of(callerVendor));

            mockMvc.perform(get("/api/v1/orders/" + orderUuid)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/by-reservation/{reservationId}")
    class GetByReservationIdTests {

        @Test
        @DisplayName("should return order linked to reservation ID")
        void getByReservationId_success() throws Exception {
            Long reservationId = 99L;
            String callerSubject = "auth|vendor-user";
            Long vendorId = 5L;

            Order order = Order.builder()
                    .id(10L)
                    .uuid(UUID.randomUUID())
                    .vendorId(vendorId)
                    .reservationId(reservationId)
                    .status(OrderStatus.VALIDATED)
                    .build();

            User user = User.builder().id(1L).externalId(callerSubject).build();
            Vendor vendor = Vendor.builder().id(vendorId).userId(1L).build();

            when(getOrderUseCase.getByReservationId(reservationId)).thenReturn(Optional.of(order));
            when(userRepositoryPort.findByExternalId(callerSubject)).thenReturn(Optional.of(user));
            when(vendorRepositoryPort.findByUserId(1L)).thenReturn(Optional.of(vendor));

            mockMvc.perform(get("/api/v1/orders/by-reservation/" + reservationId)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("VALIDATED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/vendor/{vendorUuid}")
    class ListByVendorTests {

        @Test
        @DisplayName("should list orders for vendor successfully")
        void listByVendor_success() throws Exception {
            UUID vendorUuid = UUID.randomUUID();
            String callerSubject = "auth|vendor-user";
            
            Order order = Order.builder()
                    .id(10L)
                    .uuid(UUID.randomUUID())
                    .status(OrderStatus.PENDING)
                    .build();

            when(listOrdersUseCase.listByVendor(eq(vendorUuid), any(), any(), eq(callerSubject)))
                    .thenReturn(List.of(order));

            mockMvc.perform(get("/api/v1/orders/vendor/" + vendorUuid)
                            .header("Authorization", "Bearer " + buildJwt("vendor", callerSubject)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }
    }
}
