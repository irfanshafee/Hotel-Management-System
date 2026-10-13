package com.hotelbooking.hotel_booking.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.hotelbooking.hotel_booking.entity.SignupOtpConfiguration;
import com.hotelbooking.hotel_booking.repository.SignupOtpConfigurationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignupOtpConfigurationServiceTest {
    private final SignupOtpConfigurationRepository repository =
            mock(SignupOtpConfigurationRepository.class);
    private final SignupOtpConfigurationService service = new SignupOtpConfigurationService(
            repository, mock(PlatformTransactionManager.class));

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
    void returnsDatabaseValueWhenRowIsValid() {
        SignupOtpConfiguration row = row(7);
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));

        assertEquals(Duration.ofMinutes(7), service.getOtpValidity());
        assertTrue(logs.list.isEmpty());
    }

    @Test
    void returnsDefaultWhenRowIsMissing() {
        when(repository.findById((short) 1)).thenReturn(Optional.empty());

        assertEquals(Duration.ofSeconds(300), service.getOtpValidity());
        assertWarningLogged();
    }

    @Test
    void returnsDefaultWhenValueIsNull() {
        SignupOtpConfiguration row = row(null);
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));

        assertEquals(Duration.ofSeconds(300), service.getOtpValidity());
        assertWarningLogged();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, 1441, Integer.MAX_VALUE})
    void returnsDefaultWhenValueIsOutOfRange(int minutes) {
        SignupOtpConfiguration row = row(minutes);
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));

        assertEquals(Duration.ofSeconds(300), service.getOtpValidity());
        assertWarningLogged();
    }

    @Test
    void returnsDefaultWhenTableCannotBeRead() {
        when(repository.findById((short) 1)).thenThrow(new InvalidDataAccessResourceUsageException(
                "relation \"Production\".signup_otp_configuration does not exist"));

        assertEquals(Duration.ofSeconds(300), service.getOtpValidity());
        assertWarningLogged();
    }

    private void assertWarningLogged() {
        assertTrue(logs.list.stream().anyMatch(event -> event.getLevel() == Level.WARN));
    }

    private static SignupOtpConfiguration row(Integer validityMinutes) {
        SignupOtpConfiguration configuration = mock(SignupOtpConfiguration.class);
        when(configuration.getValidityMinutes()).thenReturn(validityMinutes);
        return configuration;
    }
}
