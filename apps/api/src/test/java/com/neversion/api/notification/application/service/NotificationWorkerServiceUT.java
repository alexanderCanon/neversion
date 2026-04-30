package com.neversion.api.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neversion.api.notification.application.service.template.NotificationTemplateResolver;
import com.neversion.api.notification.application.service.template.NotificationTemplateResolver.TemplateSpec;
import com.neversion.api.shared.port.out.EmailSenderPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.shared.port.out.NotificationLogPort.PendingNotification;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerServiceUT {

    @Mock
    private NotificationLogPort notificationLogPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private NotificationTemplateResolver templateResolver;

    @InjectMocks
    private NotificationWorkerService service;

    @Test
    @DisplayName("processNextBatch_happyPath_sendsEmailAndMarksSent")
    void processNextBatch_happyPath_sendsEmailAndMarksSent() {
        PendingNotification pending = new PendingNotification(
                1L, "CLIENT_WELCOME", "client@test.com", "{\"clientName\":\"Juan\"}");
        TemplateSpec spec = new TemplateSpec("¡Bienvenido!", "<html>Welcome</html>", false);

        when(notificationLogPort.findPending(anyInt())).thenReturn(List.of(pending));
        when(templateResolver.resolve("CLIENT_WELCOME", "{\"clientName\":\"Juan\"}")).thenReturn(spec);

        int result = service.processNextBatch(10);

        assertEquals(1, result);
        verify(emailSenderPort).send("client@test.com", "¡Bienvenido!", "<html>Welcome</html>");
        verify(notificationLogPort).markSent(1L);
        verify(notificationLogPort, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("processNextBatch_emptyQueue_returnsZero")
    void processNextBatch_emptyQueue_returnsZero() {
        when(notificationLogPort.findPending(anyInt())).thenReturn(List.of());

        int result = service.processNextBatch(10);

        assertEquals(0, result);
        verify(emailSenderPort, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("processNextBatch_sendFailure_marksFailed")
    void processNextBatch_sendFailure_marksFailed() {
        PendingNotification pending = new PendingNotification(
                2L, "PAYMENT_APPROVED", "client@test.com", "{}");
        TemplateSpec spec = new TemplateSpec("Pago aprobado", "<html>Approved</html>", false);

        when(notificationLogPort.findPending(anyInt())).thenReturn(List.of(pending));
        when(templateResolver.resolve("PAYMENT_APPROVED", "{}")).thenReturn(spec);
        doThrow(new EmailSenderPort.EmailSendException("API Error", new RuntimeException("timeout")))
                .when(emailSenderPort).send(any(), any(), any());

        int result = service.processNextBatch(10);

        assertEquals(0, result);
        verify(notificationLogPort, never()).markSent(2L);
        verify(notificationLogPort).markFailed(eq(2L), anyString());
    }

    @Test
    @DisplayName("processNextBatch_internalType_marksSentWithoutSending")
    void processNextBatch_internalType_marksSentWithoutSending() {
        PendingNotification pending = new PendingNotification(
                3L, "NO_INVENTORY_ALERT", "vendor:1", "{\"vendorId\":1}");
        TemplateSpec skipSpec = TemplateSpec.skip();

        when(notificationLogPort.findPending(anyInt())).thenReturn(List.of(pending));
        when(templateResolver.resolve("NO_INVENTORY_ALERT", "{\"vendorId\":1}")).thenReturn(skipSpec);

        int result = service.processNextBatch(10);

        assertEquals(1, result);
        verify(emailSenderPort, never()).send(any(), any(), any());
        verify(notificationLogPort).markSent(3L);
    }

    @Test
    @DisplayName("processNextBatch_multiplePending_processesAll")
    void processNextBatch_multiplePending_processesAll() {
        PendingNotification p1 = new PendingNotification(1L, "CLIENT_WELCOME", "a@test.com", "{}");
        PendingNotification p2 = new PendingNotification(2L, "PAYMENT_APPROVED", "b@test.com", "{}");

        TemplateSpec spec1 = new TemplateSpec("Welcome", "<html>1</html>", false);
        TemplateSpec spec2 = new TemplateSpec("Approved", "<html>2</html>", false);

        when(notificationLogPort.findPending(anyInt())).thenReturn(List.of(p1, p2));
        when(templateResolver.resolve("CLIENT_WELCOME", "{}")).thenReturn(spec1);
        when(templateResolver.resolve("PAYMENT_APPROVED", "{}")).thenReturn(spec2);

        int result = service.processNextBatch(10);

        assertEquals(2, result);
        verify(emailSenderPort, times(2)).send(any(), any(), any());
        verify(notificationLogPort).markSent(1L);
        verify(notificationLogPort).markSent(2L);
    }
}
