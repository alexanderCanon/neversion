package com.neversion.api.notification.infrastructure.adapters.out.grpc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class GrpcNotificationLogAdapterUT {

    private GrpcNotificationLogAdapter adapter;

    @Mock
    private NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() {
        adapter = new GrpcNotificationLogAdapter("localhost", 50051);
        // Inject the mock stub manually
        ReflectionTestUtils.setField(adapter, "blockingStub", blockingStub);
    }

    @Test
    @DisplayName("record_happyPath_callsGrpcSendNotification")
    void record_happyPath_callsGrpcSendNotification() {
        NotificationResponse response = NotificationResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Sent")
                .setNotificationUuid("abc-123")
                .build();
        when(blockingStub.sendNotification(any(NotificationRequest.class))).thenReturn(response);

        adapter.record("CLIENT_WELCOME", "test@example.com", "{}");

        verify(blockingStub).sendNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("record_grpcException_failsSilently")
    void record_grpcException_failsSilently() {
        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
                .when(blockingStub).sendNotification(any(NotificationRequest.class));

        // Should not throw exception
        adapter.record("CLIENT_WELCOME", "test@example.com", "{}");

        verify(blockingStub).sendNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("existsByEntityAndStage_exists_returnsTrue")
    void existsByEntityAndStage_exists_returnsTrue() {
        CheckExistsResponse response = CheckExistsResponse.newBuilder()
                .setExists(true)
                .build();
        when(blockingStub.checkExists(any(CheckExistsRequest.class))).thenReturn(response);

        boolean exists = adapter.existsByEntityAndStage("subscription", 1L, "reminder_7d");

        assertTrue(exists);
        verify(blockingStub).checkExists(any(CheckExistsRequest.class));
    }

    @Test
    @DisplayName("existsByEntityAndStage_notExists_returnsFalse")
    void existsByEntityAndStage_notExists_returnsFalse() {
        CheckExistsResponse response = CheckExistsResponse.newBuilder()
                .setExists(false)
                .build();
        when(blockingStub.checkExists(any(CheckExistsRequest.class))).thenReturn(response);

        boolean exists = adapter.existsByEntityAndStage("subscription", 1L, "reminder_7d");

        assertFalse(exists);
        verify(blockingStub).checkExists(any(CheckExistsRequest.class));
    }

    @Test
    @DisplayName("existsByEntityAndStage_grpcException_returnsFalse")
    void existsByEntityAndStage_grpcException_returnsFalse() {
        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
                .when(blockingStub).checkExists(any(CheckExistsRequest.class));

        boolean exists = adapter.existsByEntityAndStage("subscription", 1L, "reminder_7d");

        assertFalse(exists);
        verify(blockingStub).checkExists(any(CheckExistsRequest.class));
    }
}
