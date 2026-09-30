package com.hotelbooking.hotel_booking.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads flat key/value configuration from {@code .env/<profile>.json} early
 * enough for datasource auto-configuration. The property source is placed
 * below real OS environment variables so deployment-time secrets always win.
 */
public final class JsonEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered {

    private static final Set<String> JSON_PROFILES = Set.of("local", "prod");
    private static final String PROPERTY_SOURCE_PREFIX = "profileJson:";

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            SpringApplication application) {
        String profile = resolveJsonProfile(environment);
        if (profile == null) {
            return;
        }

        String location = ".env/" + profile + ".json";
        Resource resource = new ClassPathResource(location);
        if (!resource.exists()) {
            return;
        }

        Map<String, Object> values = readFlatJson(resource, location);
        if (values.isEmpty()) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        MapPropertySource jsonSource = new MapPropertySource(
                PROPERTY_SOURCE_PREFIX + profile, values);

        if (propertySources.contains(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
            propertySources.addAfter(
                    StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    jsonSource);
        } else {
            propertySources.addLast(jsonSource);
        }
    }

    @Override
    public int getOrder() {
        // Profiles and YAML config data must be known before choosing a JSON file.
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    private String resolveJsonProfile(ConfigurableEnvironment environment) {
        Set<String> matchingProfiles = new LinkedHashSet<>();
        addMatchingProfiles(matchingProfiles, environment.getActiveProfiles());
        if (matchingProfiles.isEmpty()) {
            addMatchingProfiles(matchingProfiles, environment.getDefaultProfiles());
        }
        if (matchingProfiles.size() > 1) {
            throw new IllegalStateException(
                    "Only one database profile may be active: local or prod");
        }
        return matchingProfiles.stream().findFirst().orElse(null);
    }

    private void addMatchingProfiles(Set<String> matches, String[] profiles) {
        Arrays.stream(profiles)
                .filter(JSON_PROFILES::contains)
                .forEach(matches::add);
    }

    private Map<String, Object> readFlatJson(Resource resource, String location) {
        try (var input = resource.getInputStream()) {
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> parsed = JsonParserFactory.getJsonParser()
                    .parseMap(json);
            Map<String, Object> values = new LinkedHashMap<>();
            parsed.forEach((key, value) -> {
                if (key == null || key.isBlank()) {
                    throw new IllegalStateException(
                            "Blank configuration key in " + location);
                }
                if (value == null || value instanceof Map<?, ?>
                        || value instanceof Iterable<?>) {
                    throw new IllegalStateException(
                            "Only non-null scalar values are allowed in "
                                    + location + " (invalid key: " + key + ")");
                }
                values.put(key, String.valueOf(value));
            });
            return values;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Unable to load JSON environment configuration from "
                            + location,
                    exception);
        }
    }
}
