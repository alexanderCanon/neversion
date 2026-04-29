package com.neversion.api.assignment.application.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
class NotificationPayloadWriter {

    private final ObjectMapper objectMapper;

    NotificationPayloadWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String write(Map<String, ?> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize notification payload", ex);
        }
    }
}
