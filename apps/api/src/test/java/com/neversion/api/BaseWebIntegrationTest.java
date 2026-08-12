package com.neversion.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.ConfirmAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.ManualAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.SuggestAssignmentUseCase;
import com.neversion.api.client.application.port.in.ClientUseCase;
import com.neversion.api.game.application.port.in.GameUseCase;
import com.neversion.api.gamesku.application.port.in.GameSkuUseCase;
import com.neversion.api.order.application.port.in.ChangeOrderStatusUseCase;
import com.neversion.api.order.application.port.in.GetOrderUseCase;
import com.neversion.api.order.application.port.in.ListOrdersUseCase;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
import com.neversion.api.reservation.application.port.in.CancelReservationUseCase;
import com.neversion.api.reservation.application.port.in.CreateRenewalReservationUseCase;
import com.neversion.api.reservation.application.port.in.CreateReservationUseCase;
import com.neversion.api.reservation.application.port.in.RejectReservationUseCase;
import com.neversion.api.reservation.application.port.in.UploadReceiptUseCase;
import com.neversion.api.reservation.application.port.in.ValidateReservationUseCase;

import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;
import com.neversion.api.service.application.port.in.ServiceUseCase;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase;
import com.neversion.api.subscription.application.port.in.ListSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.RenewSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.RevokeSubscriptionUseCase;
import com.neversion.api.subscription.application.port.in.UpdateSubscriptionUseCase;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.application.port.in.GetCurrentUserContextUseCase;
import com.neversion.api.user.application.port.in.RegisterClientUseCase;
import com.neversion.api.user.application.port.in.RegisterVendorUseCase;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base integration test for REST Controllers and Web Security Layer.
 * <p>
 * Consolidates all `@MockitoBean` declarations so Spring Boot creates and caches
 * a single shared `ApplicationContext` across all REST controller ITs, avoiding
 * repeated context reboots.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseWebIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ─── Auth Mocks ─────────────────────────────────────────────────────────
    @MockitoBean protected RegisterVendorUseCase registerVendorUseCase;
    @MockitoBean protected RegisterClientUseCase registerClientUseCase;
    @MockitoBean protected GetCurrentUserContextUseCase getCurrentUserContextUseCase;

    // ─── Client Mocks ────────────────────────────────────────────────────────
    @MockitoBean protected ClientUseCase clientUseCase;

    // ─── Service / Game Mocks ────────────────────────────────────────────────
    @MockitoBean protected ServiceUseCase serviceUseCase;
    @MockitoBean protected GameUseCase gameUseCase;
    @MockitoBean protected GameSkuUseCase gameSkuUseCase;

    // ─── Order Mocks ─────────────────────────────────────────────────────────
    @MockitoBean protected ChangeOrderStatusUseCase changeOrderStatusUseCase;
    @MockitoBean protected GetOrderUseCase getOrderUseCase;
    @MockitoBean protected ListOrdersUseCase listOrdersUseCase;
    @MockitoBean protected OrderStatusHistoryPort orderStatusHistoryPort;

    // ─── Reservation Mocks ───────────────────────────────────────────────────
    @MockitoBean protected CreateReservationUseCase createReservationUseCase;
    @MockitoBean protected CancelReservationUseCase cancelReservationUseCase;
    @MockitoBean protected CreateRenewalReservationUseCase createRenewalReservationUseCase;
    @MockitoBean protected ValidateReservationUseCase validateReservationUseCase;
    @MockitoBean protected RejectReservationUseCase rejectReservationUseCase;
    @MockitoBean protected UploadReceiptUseCase uploadReceiptUseCase;
    @MockitoBean protected ReservationRepositoryPort reservationRepositoryPort;

    // ─── Assignment Mocks ────────────────────────────────────────────────────
    @MockitoBean protected SuggestAssignmentUseCase suggestAssignmentUseCase;
    @MockitoBean protected ConfirmAssignmentUseCase confirmAssignmentUseCase;
    @MockitoBean protected ManualAssignmentUseCase manualAssignmentUseCase;

    // ─── Subscription Mocks ──────────────────────────────────────────────────
    @MockitoBean protected CreateManualSubscriptionUseCase createManualSubscriptionUseCase;
    @MockitoBean protected UpdateSubscriptionUseCase updateSubscriptionUseCase;
    @MockitoBean protected ListSubscriptionsUseCase listSubscriptionsUseCase;
    @MockitoBean protected GetSubscriptionDetailUseCase getSubscriptionDetailUseCase;
    @MockitoBean protected RenewSubscriptionUseCase renewSubscriptionUseCase;
    @MockitoBean protected RevokeSubscriptionUseCase revokeSubscriptionUseCase;
    @MockitoBean protected DetectExpiredSubscriptionsUseCase detectExpiredSubscriptionsUseCase;
    @MockitoBean protected SubscriptionRepositoryPort subscriptionRepositoryPort;

    // ─── Core Repositories Mocks (used by controllers for authorization/context) ─
    @MockitoBean protected UserRepositoryPort userRepositoryPort;
    @MockitoBean protected VendorRepositoryPort vendorRepositoryPort;
    @MockitoBean protected AccountRepositoryPort accountRepositoryPort;

    @BeforeEach
    void resetWebMocks() {
        Mockito.reset(
                registerVendorUseCase, registerClientUseCase, getCurrentUserContextUseCase,
                clientUseCase, serviceUseCase, gameUseCase, gameSkuUseCase,
                changeOrderStatusUseCase, getOrderUseCase, listOrdersUseCase, orderStatusHistoryPort,
                createReservationUseCase, cancelReservationUseCase, createRenewalReservationUseCase,
                validateReservationUseCase, rejectReservationUseCase, uploadReceiptUseCase,
                reservationRepositoryPort, suggestAssignmentUseCase,
                confirmAssignmentUseCase, manualAssignmentUseCase, createManualSubscriptionUseCase,
                updateSubscriptionUseCase, listSubscriptionsUseCase, getSubscriptionDetailUseCase,
                renewSubscriptionUseCase, revokeSubscriptionUseCase, detectExpiredSubscriptionsUseCase,
                subscriptionRepositoryPort, userRepositoryPort, vendorRepositoryPort, accountRepositoryPort
        );
    }

}
