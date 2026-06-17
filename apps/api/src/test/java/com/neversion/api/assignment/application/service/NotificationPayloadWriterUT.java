package com.neversion.api.assignment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPayloadWriter Unit Tests")
class NotificationPayloadWriterUT {

    private ObjectMapper objectMapper;
    private NotificationPayloadWriter writer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        writer = new NotificationPayloadWriter(objectMapper);
    }

    @Test
    @DisplayName("write - should serialize map to JSON string successfully")
    void write_shouldSerializeSuccessfully() {
        Map<String, String> payload = Map.of("key", "value", "name", "Alex");
        
        String json = writer.write(payload);
        
        assertThat(json).contains("\"key\":\"value\"");
        assertThat(json).contains("\"name\":\"Alex\"");
    }

    @Test
    @DisplayName("write - should throw IllegalStateException when ObjectMapper serialization throws JsonProcessingException")
    void write_shouldThrowIllegalStateException_whenSerializationFails() throws Exception {
        ObjectMapper mockedMapper = mock(ObjectMapper.class);
        NotificationPayloadWriter mockedWriter = new NotificationPayloadWriter(mockedMapper);
        
        // Use a valid subclass of JsonProcessingException
        JsonProcessingException ex = new JsonMappingException(null, "Mocked serialization error");
        when(mockedMapper.writeValueAsString(any())).thenThrow(ex);
        
        assertThatThrownBy(() -> mockedWriter.write(Map.of("key", "val")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to serialize notification payload")
                .hasCause(ex);
    }
}
