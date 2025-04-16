package com.servx.servx.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.servx.servx.entity.NotificationPayload;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationPayloadConverter implements AttributeConverter<NotificationPayload, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public String convertToDatabaseColumn(NotificationPayload attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting payload to JSON", e);
        }
    }

    @Override
    public NotificationPayload convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, NotificationPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to payload", e);
        }
    }
}