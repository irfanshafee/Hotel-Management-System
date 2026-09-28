package com.hotelbooking.hotel_booking.exception;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

@Component
public class ExceptionMessageCatalog {
    private static final String CATALOG_PATH = "exception-messages.json";

    private final Map<String, MessageDefinition> definitions;

    public ExceptionMessageCatalog(ObjectMapper objectMapper) {
        try (var inputStream = new ClassPathResource(CATALOG_PATH).getInputStream()) {
            definitions = objectMapper.readValue(inputStream, new TypeReference<>() { });
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + CATALOG_PATH, exception);
        }
    }

    public ResolvedMessage resolve(String key, Object... arguments) {
        MessageDefinition definition = definitions.get(key);
        if (definition == null) {
            throw new IllegalStateException("No exception message configured for key: " + key);
        }

        HttpStatus status = HttpStatus.resolve(definition.status());
        if (status == null) {
            throw new IllegalStateException(
                    "Invalid HTTP status configured for exception message key: " + key);
        }
        return new ResolvedMessage(
                status, MessageFormat.format(definition.message(), arguments));
    }

    private record MessageDefinition(int status, String message) { }

    public record ResolvedMessage(HttpStatus status, String message) { }
}
