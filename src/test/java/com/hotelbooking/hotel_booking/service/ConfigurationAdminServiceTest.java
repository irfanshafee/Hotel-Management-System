package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.ConfigurationResponse;
import com.hotelbooking.hotel_booking.entity.ConfigurationEntry;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.ConfigurationEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurationAdminServiceTest {
    private static final String OTP_KEY = "signup.otp.validity.minutes";

    private final ConfigurationEntryRepository repository = mock(ConfigurationEntryRepository.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final ConfigurationAdminService service =
            new ConfigurationAdminService(repository, configurationService);

    @Test
    void updateSavesNewValueThenEvictsThatKey() {
        ConfigurationEntry entry = entry(OTP_KEY, "5");
        when(repository.findById(OTP_KEY)).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurationResponse response = service.update(OTP_KEY, " 10 ");

        assertEquals("10", response.value());
        assertNotNull(response.updatedAt());
        InOrder order = inOrder(repository, configurationService);
        order.verify(repository).save(entry);
        order.verify(configurationService).evict(OTP_KEY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1441", "abc", "5.5"})
    void updateRejectsInvalidValueForKnownKey(String value) {
        ApiException error = assertThrows(ApiException.class, () -> service.update(OTP_KEY, value));

        assertEquals("configuration.value.invalid", error.getMessageKey());
        assertArrayEquals(new Object[]{OTP_KEY, "1", "1440"}, error.getMessageArguments());
        verify(repository, never()).save(any());
        verify(configurationService, never()).evict(anyString());
    }

    @Test
    void updateCreatesMissingRowForKnownKey() {
        when(repository.findById(OTP_KEY)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(OTP_KEY, "8");

        ArgumentCaptor<ConfigurationEntry> saved = ArgumentCaptor.forClass(ConfigurationEntry.class);
        verify(repository).save(saved.capture());
        assertEquals("8", saved.getValue().getConfigValue());
        assertNotNull(saved.getValue().getDescription());
    }

    @Test
    void updateRejectsUnknownKeyWithoutRow() {
        when(repository.findById("no.such.key")).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> service.update("no.such.key", "x"));

        assertEquals("configuration.not-found", error.getMessageKey());
        verify(repository, never()).save(any());
    }

    @Test
    void updateAllowsAnyValueForUnknownKeyThatExists() {
        ConfigurationEntry entry = entry("feature.banner.text", "old");
        when(repository.findById("feature.banner.text")).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("Welcome!", service.update("feature.banner.text", "Welcome!").value());
        verify(configurationService).evict("feature.banner.text");
    }

    @Test
    void getReturnsRowOrNotFound() {
        when(repository.findById(OTP_KEY)).thenReturn(Optional.of(entry(OTP_KEY, "5")));
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertEquals("5", service.get(OTP_KEY).value());
        assertEquals("configuration.not-found",
                assertThrows(ApiException.class, () -> service.get("missing")).getMessageKey());
    }

    @Test
    void getAllListsRowsSortedByKey() {
        when(repository.findAll(Sort.by("configKey"))).thenReturn(List.of(entry(OTP_KEY, "5")));

        assertEquals(List.of(OTP_KEY), service.getAll().stream().map(ConfigurationResponse::key).toList());
    }

    private static ConfigurationEntry entry(String key, String value) {
        ConfigurationEntry entry = new ConfigurationEntry(key, "description");
        entry.updateValue(value, LocalDateTime.now().minusDays(1));
        return entry;
    }
}
