package com.hotelbooking.hotel_booking.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignupOtpConfigurationServiceTest {
    private static final String KEY = "signup.otp.validity.minutes";

    private final ConfigurationService configuration = mock(ConfigurationService.class);
    private final SignupOtpConfigurationService service = new SignupOtpConfigurationService(configuration);

    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger serviceLogger =
            (Logger) LoggerFactory.getLogger(SignupOtpConfigurationService.class);

    @BeforeEach
    void captureLogs() {
        logs.start();
        serviceLogger.addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        serviceLogger.detachAppender(logs);
    }

    @Test
    void returnsConfiguredValueReadByKey() {
        when(configuration.getValue(KEY)).thenReturn("7");

        assertEquals(Duration.ofMinutes(7), service.getOtpValidity());
        assertTrue(logs.list.isEmpty());
    }

    @Test
    void acceptsSurroundingWhitespace() {
        when(configuration.getValue(KEY)).thenReturn(" 12 ");

        assertEquals(Duration.ofMinutes(12), service.getOtpValidity());
    }

    @Test
    void returnsDefaultWhenKeyIsMissing() {
        when(configuration.getValue(KEY)).thenReturn(null);

        assertEquals(Duration.ofMinutes(5), service.getOtpValidity());
        assertWarningLogged();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "1441", "2147483648", "abc", "5.5", ""})
    void returnsDefaultWhenValueIsInvalid(String value) {
        when(configuration.getValue(KEY)).thenReturn(value);

        assertEquals(Duration.ofMinutes(5), service.getOtpValidity());
        assertWarningLogged();
    }

    private void assertWarningLogged() {
        assertTrue(logs.list.stream().anyMatch(event -> event.getLevel() == Level.WARN));
    }
}
