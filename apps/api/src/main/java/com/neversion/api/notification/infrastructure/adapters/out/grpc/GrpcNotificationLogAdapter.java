package com.neversion.api.notification.infrastructure.adapters.out.grpc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.neversion.api.shared.port.out.NotificationLogPort;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * gRPC adapter for NotificationLogPort.
 * Delegates all notification operations to the external Node.js gRPC service.
 */
@Component
@Primary
public class GrpcNotificationLogAdapter implements NotificationLogPort, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GrpcNotificationLogAdapter.class);

    private final String host;
    private final int port;
    private ManagedChannel channel;
    private NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub;

    public GrpcNotificationLogAdapter(
            @Value("${neversion.notification-service.host:localhost}") String host,
            @Value("${neversion.notification-service.port:50051}") int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing gRPC client connecting to {}:{}", host, port);
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = NotificationServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void destroy() {
        log.info("Shutting down gRPC client channel for {}:{}", host, port);
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    @Override
    public void record(String type, String recipientEmail, String payload) {
        record(type, recipientEmail, payload, "", 0L, "");
    }

    @Override
    public void record(String type, String recipientEmail, String payload,
                        String entityType, Long entityId, String stage) {
        log.info("Sending notification request via gRPC: type={}, recipient={}, referenceType={}, referenceId={}, tags={}",
                type, recipientEmail, entityType, entityId, stage);
        
        NotificationRequest request = NotificationRequest.newBuilder()
                .setEventType(type != null ? type : "")
                .setRecipient(recipientEmail != null ? recipientEmail : "")
                .setPayload(payload != null ? payload : "")
                .setReferenceType(entityType != null ? entityType : "")
                .setReferenceId(entityId != null ? entityId : 0L)
                .setTags(stage != null ? stage : "")
                .build();

        try {
            NotificationResponse response = blockingStub.sendNotification(request);
            if (!response.getSuccess()) {
                log.error("gRPC notification service failed to deliver: {}", response.getMessage());
            } else {
                log.info("gRPC notification successfully queued, UUID={}", response.getNotificationUuid());
            }
        } catch (StatusRuntimeException e) {
            log.error("gRPC invocation failed: {}", e.getStatus());
        }
    }

    @Override
    public boolean existsByEntityAndStage(String entityType, Long entityId, String stage) {
        log.info("Checking if notification exists via gRPC: referenceType={}, referenceId={}, tags={}",
                entityType, entityId, stage);

        CheckExistsRequest request = CheckExistsRequest.newBuilder()
                .setReferenceType(entityType != null ? entityType : "")
                .setReferenceId(entityId != null ? entityId : 0L)
                .setTags(stage != null ? stage : "")
                .build();

        try {
            CheckExistsResponse response = blockingStub.checkExists(request);
            return response.getExists();
        } catch (StatusRuntimeException e) {
            log.error("gRPC exists check failed: {}, returning false", e.getStatus());
            return false;
        }
    }

    @Override
    public List<PendingNotification> findPending(int limit) {
        return List.of();
    }

    @Override
    public void markSent(Long id) {
        // No-op
    }

    @Override
    public void markFailed(Long id, String errorMessage) {
        // No-op
    }
}
