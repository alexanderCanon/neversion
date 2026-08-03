package com.neversion.api.reservation.infrastructure.adapters.out.grpc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@Component
@Primary
@ConditionalOnProperty(name = "neversion.reservation-service.enabled", havingValue = "true", matchIfMissing = false)
public class GrpcReservationAdapter implements ReservationRepositoryPort, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GrpcReservationAdapter.class);

    private final String host;
    private final int port;
    private ManagedChannel channel;
    private ReservationServiceGrpc.ReservationServiceBlockingStub blockingStub;

    public GrpcReservationAdapter(
            @Value("${neversion.reservation-service.host:localhost}") String host,
            @Value("${neversion.reservation-service.port:50052}") int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing gRPC client for ReservationService connecting to {}:{}", host, port);
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = ReservationServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void destroy() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Shutting down gRPC client channel for ReservationService {}:{}", host, port);
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean existsByReceiptUrl(String receiptUrl) {
        return false;
    }

    @Override
    public boolean existsActiveRenewalBySubscriptionId(Long subscriptionId) {
        try {
            CheckActiveRenewalRequest req = CheckActiveRenewalRequest.newBuilder()
                    .setSubscriptionId(subscriptionId != null ? subscriptionId : 0L)
                    .build();
            CheckActiveRenewalResponse resp = blockingStub.checkActiveRenewal(req);
            return resp.getActive();
        } catch (StatusRuntimeException e) {
            log.error("gRPC checkActiveRenewal failed: {}", e.getStatus());
            return false;
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        try {
            CreateReservationRequest.Builder builder = CreateReservationRequest.newBuilder()
                    .setClientId(reservation.getClientId() != null ? reservation.getClientId() : 0L)
                    .setClientUuid(reservation.getClientUuid() != null ? reservation.getClientUuid().toString() : "")
                    .setVendorId(reservation.getVendorId() != null ? reservation.getVendorId() : 0L)
                    .setDiscount(reservation.getDiscount() != null ? reservation.getDiscount().toString() : "0.0")
                    .setTotal(reservation.getTotal() != null ? reservation.getTotal().toString() : "0.0")
                    .setPaymentMethod(reservation.getPaymentMethod() != null ? reservation.getPaymentMethod() : "")
                    .setAccountPreference(reservation.getAccountPreference() != null ? reservation.getAccountPreference().name() : "")
                    .setNotes(reservation.getNotes() != null ? reservation.getNotes() : "")
                    .setRenewalSubscriptionId(reservation.getRenewalSubscriptionId() != null ? reservation.getRenewalSubscriptionId() : 0L)
                    .setRenewalSubscriptionUuid(reservation.getRenewalSubscriptionUuid() != null ? reservation.getRenewalSubscriptionUuid().toString() : "")
                    .setPointsRedeemed(reservation.getPointsRedeemed() != null ? reservation.getPointsRedeemed() : 0L)
                    .setPointsDiscount(reservation.getPointsDiscount() != null ? reservation.getPointsDiscount().toString() : "0.0");

            if (reservation.getDetails() != null) {
                for (ReservationDetail detail : reservation.getDetails()) {
                    builder.addDetails(ReservationDetailItem.newBuilder()
                            .setServiceId(detail.serviceId() != null ? detail.serviceId() : 0L)
                            .setQty(detail.qty() != null ? detail.qty() : 1)
                            .setUnitPrice(detail.unitPrice() != null ? detail.unitPrice().toString() : "0.0")
                            .setSubtotal(detail.subtotal() != null ? detail.subtotal().toString() : "0.0")
                            .build());
                }
            }

            ReservationResponse resp = blockingStub.createReservation(builder.build());
            return toDomain(resp);
        } catch (StatusRuntimeException e) {
            log.error("gRPC createReservation failed: {}", e.getStatus());
            throw new RuntimeException("Reservation service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Reservation update(Reservation reservation) {
        try {
            com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus protoStatus =
                    mapToProtoStatus(reservation.getStatus());

            UpdateReservationStatusRequest req = UpdateReservationStatusRequest.newBuilder()
                    .setUuid(reservation.getUuid() != null ? reservation.getUuid().toString() : "")
                    .setStatus(protoStatus)
                    .setReceiptUrl(reservation.getReceiptUrl() != null ? reservation.getReceiptUrl() : "")
                    .setNotes(reservation.getNotes() != null ? reservation.getNotes() : "")
                    .build();

            ReservationResponse resp = blockingStub.updateReservationStatus(req);
            return toDomain(resp);
        } catch (StatusRuntimeException e) {
            log.error("gRPC updateReservationStatus failed: {}", e.getStatus());
            throw new RuntimeException("Reservation service update failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Reservation> findByUuid(UUID uuid) {
        try {
            GetReservationByUuidRequest req = GetReservationByUuidRequest.newBuilder()
                    .setUuid(uuid != null ? uuid.toString() : "")
                    .build();
            ReservationResponse resp = blockingStub.getReservationByUuid(req);
            return resp.getSuccess() ? Optional.ofNullable(toDomain(resp)) : Optional.empty();
        } catch (StatusRuntimeException e) {
            log.error("gRPC findByUuid failed: {}", e.getStatus());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        try {
            GetReservationByIdRequest req = GetReservationByIdRequest.newBuilder()
                    .setId(id != null ? id : 0L)
                    .build();
            ReservationResponse resp = blockingStub.getReservationById(req);
            return resp.getSuccess() ? Optional.ofNullable(toDomain(resp)) : Optional.empty();
        } catch (StatusRuntimeException e) {
            log.error("gRPC findById failed: {}", e.getStatus());
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAll() {
        return Collections.emptyList();
    }

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        return Collections.emptyList();
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return Collections.emptyList();
    }

    @Override
    public int expirePendingReservations() {
        try {
            ExpirePendingReservationsRequest req = ExpirePendingReservationsRequest.newBuilder()
                    .setExpirationMinutes(60)
                    .build();
            ExpirePendingReservationsResponse resp = blockingStub.expirePendingReservations(req);
            return resp.getExpiredCount();
        } catch (StatusRuntimeException e) {
            log.error("gRPC expirePendingReservations failed: {}", e.getStatus());
            return 0;
        }
    }

    @Override
    public ReservationDetail saveDetail(ReservationDetail detail) {
        return detail;
    }

    @Override
    public List<ReservationDetail> findDetailsByReservationId(Long reservationId) {
        return Collections.emptyList();
    }

    private Reservation toDomain(ReservationResponse resp) {
        if (!resp.getSuccess()) {
            return null;
        }
        return Reservation.builder()
                .id(resp.getId())
                .uuid(resp.getUuid().isEmpty() ? null : UUID.fromString(resp.getUuid()))
                .clientId(resp.getClientId() == 0 ? null : resp.getClientId())
                .clientUuid(resp.getClientUuid().isEmpty() ? null : UUID.fromString(resp.getClientUuid()))
                .vendorId(resp.getVendorId() == 0 ? null : resp.getVendorId())
                .discount(resp.getDiscount().isEmpty() ? BigDecimal.ZERO : new BigDecimal(resp.getDiscount()))
                .total(resp.getTotal().isEmpty() ? BigDecimal.ZERO : new BigDecimal(resp.getTotal()))
                .receiptUrl(resp.getReceiptUrl().isEmpty() ? null : resp.getReceiptUrl())
                .paymentMethod(resp.getPaymentMethod().isEmpty() ? null : resp.getPaymentMethod())
                .status(mapFromProtoStatus(resp.getStatus()))
                .expirationDate(parseTimestamp(resp.getExpirationDate()))
                .createdAt(parseTimestamp(resp.getCreatedAt()))
                .notes(resp.getNotes().isEmpty() ? null : resp.getNotes())
                .renewalSubscriptionId(resp.getRenewalSubscriptionId() == 0 ? null : resp.getRenewalSubscriptionId())
                .renewalSubscriptionUuid(resp.getRenewalSubscriptionUuid().isEmpty() ? null : UUID.fromString(resp.getRenewalSubscriptionUuid()))
                .pointsRedeemed(resp.getPointsRedeemed())
                .pointsDiscount(resp.getPointsDiscount().isEmpty() ? BigDecimal.ZERO : new BigDecimal(resp.getPointsDiscount()))
                .build();
    }

    private Instant parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) {
            return null;
        }
        try {
            String formatted = timestampStr.replace(" ", "T");
            if (!formatted.endsWith("Z") && !formatted.contains("+")) {
                formatted += "Z";
            }
            return Instant.parse(formatted);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp '{}': {}", timestampStr, e.getMessage());
            return Instant.now();
        }
    }

    private com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus mapToProtoStatus(ReservationStatus status) {
        if (status == null) return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.PENDING;
        switch (status) {
            case PENDING: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.PENDING;
            case UPLOADED: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.UPLOADED;
            case VALIDATED: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.VALIDATED;
            case REJECTED: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.REJECTED;
            case EXPIRED: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.EXPIRED;
            case CANCELLED: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.CANCELLED;
            default: return com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus.PENDING;
        }
    }

    private ReservationStatus mapFromProtoStatus(com.neversion.api.reservation.infrastructure.adapters.out.grpc.ReservationStatus status) {
        if (status == null) return ReservationStatus.PENDING;
        switch (status) {
            case PENDING: return ReservationStatus.PENDING;
            case UPLOADED: return ReservationStatus.UPLOADED;
            case VALIDATED: return ReservationStatus.VALIDATED;
            case REJECTED: return ReservationStatus.REJECTED;
            case EXPIRED: return ReservationStatus.EXPIRED;
            case CANCELLED: return ReservationStatus.CANCELLED;
            default: return ReservationStatus.PENDING;
        }
    }
}
