package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.SignupOtpSettingsResponse;
import com.hotelbooking.hotel_booking.entity.SignupOtpConfiguration;
import com.hotelbooking.hotel_booking.repository.SignupOtpConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignupOtpSettingsAdminServiceTest {
    private final SignupOtpConfigurationRepository repository =
            mock(SignupOtpConfigurationRepository.class);
    private final SignupOtpConfigurationService configurationService =
            mock(SignupOtpConfigurationService.class);
    private final SignupOtpSettingsAdminService service =
            new SignupOtpSettingsAdminService(repository, configurationService);

    @Test
    void updateSavesNewValueThenEvictsCache() {
        SignupOtpConfiguration row = new SignupOtpConfiguration((short) 1);
        row.updateValidityMinutes(5, LocalDateTime.now().minusDays(1));
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SignupOtpSettingsResponse response = service.updateValidityMinutes(10);

        assertEquals(10, response.validityMinutes());
        assertNotNull(response.updatedAt());
        InOrder order = inOrder(repository, configurationService);
        order.verify(repository).save(row);
        order.verify(configurationService).evictOtpValidity();
    }

    @Test
    void updateCreatesTheRowWhenMissing() {
        when(repository.findById((short) 1)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateValidityMinutes(7);

        ArgumentCaptor<SignupOtpConfiguration> saved =
                ArgumentCaptor.forClass(SignupOtpConfiguration.class);
        org.mockito.Mockito.verify(repository).save(saved.capture());
        assertEquals(7, saved.getValue().getValidityMinutes());
    }

    @Test
    void getReturnsDatabaseRow() {
        SignupOtpConfiguration row = new SignupOtpConfiguration((short) 1);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 10, 1, 12, 0);
        row.updateValidityMinutes(8, updatedAt);
        when(repository.findById((short) 1)).thenReturn(Optional.of(row));

        SignupOtpSettingsResponse response = service.getSettings();

        assertEquals(8, response.validityMinutes());
        assertEquals(updatedAt, response.updatedAt());
    }

    @Test
    void getReturnsEffectiveDefaultWhenRowIsMissing() {
        when(repository.findById((short) 1)).thenReturn(Optional.empty());
        when(configurationService.getOtpValidity()).thenReturn(Duration.ofMinutes(5));

        SignupOtpSettingsResponse response = service.getSettings();

        assertEquals(5, response.validityMinutes());
        assertNull(response.updatedAt());
    }
}
