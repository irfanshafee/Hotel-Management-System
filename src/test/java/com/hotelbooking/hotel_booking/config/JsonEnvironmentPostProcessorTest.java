package com.hotelbooking.hotel_booking.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonEnvironmentPostProcessorTest {

    private final JsonEnvironmentPostProcessor processor =
            new JsonEnvironmentPostProcessor();

    @Test
    void loadsJsonForActiveProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");

        processor.postProcessEnvironment(
                environment, new SpringApplication(Object.class));

        assertEquals("loaded-from-local-json",
                environment.getProperty("JSON_LOADER_TEST_VALUE"));
    }

    @Test
    void operatingSystemEnvironmentHasHigherPrecedenceThanJson() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        environment.getPropertySources().addFirst(new MockPropertySource(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
                .withProperty("JSON_LOADER_OVERRIDE_TEST", "os-value"));

        processor.postProcessEnvironment(
                environment, new SpringApplication(Object.class));

        assertEquals("os-value",
                environment.getProperty("JSON_LOADER_OVERRIDE_TEST"));
    }

    @Test
    void ignoresProfilesWithoutJsonConfiguration() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");

        processor.postProcessEnvironment(
                environment, new SpringApplication(Object.class));

        assertNull(environment.getProperty("JSON_LOADER_TEST_VALUE"));
    }

    @Test
    void doesNotLoadJsonForProductionProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        processor.postProcessEnvironment(
                environment, new SpringApplication(Object.class));

        assertNull(environment.getProperty("JSON_LOADER_TEST_VALUE"));
    }
}
