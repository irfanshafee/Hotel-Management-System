package com.hotelbooking.hotel_booking.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

@Component
public class ApiSuccessMessageCatalog {
    private static final String CATALOG_PATH = "api-success-messages.json";

    private final Map<String, String> messages;

    public ApiSuccessMessageCatalog(ObjectMapper objectMapper) {
        try (var inputStream = new ClassPathResource(CATALOG_PATH).getInputStream()) {
            messages = objectMapper.readValue(inputStream, new TypeReference<>() { });
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + CATALOG_PATH, exception);
        }
    }

    public String get(String key, Object... arguments) {
        String message = messages.get(key);
        if (message == null) {
            throw new IllegalStateException("No API success message configured for key: " + key);
        }
        return MessageFormat.format(message, arguments);
    }
}
