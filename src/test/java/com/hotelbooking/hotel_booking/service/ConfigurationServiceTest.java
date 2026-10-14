package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.ConfigurationEntry;
import com.hotelbooking.hotel_booking.repository.ConfigurationEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurationServiceTest {
    private final ConfigurationEntryRepository repository = mock(ConfigurationEntryRepository.class);
    private final ConfigurationService service = new ConfigurationService(repository);

    @Test
    void returnsStoredValue() {
        ConfigurationEntry entry = new ConfigurationEntry("some.key", null);
        entry.updateValue("42", LocalDateTime.now());
        when(repository.findById("some.key")).thenReturn(Optional.of(entry));

        assertEquals("42", service.getValue("some.key"));
    }

    @Test
    void returnsNullWhenKeyIsMissing() {
        when(repository.findById("some.key")).thenReturn(Optional.empty());

        assertNull(service.getValue("some.key"));
    }

    @Test
    void databaseErrorPropagatesToCaller() {
        when(repository.findById("some.key")).thenThrow(new InvalidDataAccessResourceUsageException(
                "relation \"Production\".configuration_table does not exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class, () -> service.getValue("some.key"));
    }
}
