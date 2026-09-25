package com.hotelbooking.hotel_booking.security;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class OpenApiRegistry {
    private static final String CONFIG_LOCATION = "security/open_apis.json";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<OpenApiRule> openApis;

    public OpenApiRegistry(ObjectMapper objectMapper) {
        this.openApis = loadOpenApis(objectMapper);
    }

    public boolean isPublic(String method, String path) {
        return openApis.stream().anyMatch(rule ->
                rule.method().equalsIgnoreCase(method)
                        && pathMatcher.match(rule.path(), path));
    }

    private List<OpenApiRule> loadOpenApis(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource(CONFIG_LOCATION);

        try (InputStream inputStream = resource.getInputStream()) {
            OpenApiRule[] rules = objectMapper.readValue(inputStream, OpenApiRule[].class);
            return Arrays.stream(rules)
                    .map(this::validateAndNormalize)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load public API configuration: " + CONFIG_LOCATION, exception);
        }
    }

    private OpenApiRule validateAndNormalize(OpenApiRule rule) {
        if (rule == null || rule.method() == null || rule.method().isBlank()
                || rule.path() == null || rule.path().isBlank()
                || !rule.path().startsWith("/")) {
            throw new IllegalStateException(
                    "Invalid public API rule in " + CONFIG_LOCATION);
        }

        return new OpenApiRule(
                rule.method().trim().toUpperCase(Locale.ROOT),
                rule.path().trim());
    }

    public record OpenApiRule(String method, String path) {
    }
}
