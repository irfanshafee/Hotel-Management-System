package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.SignupOtpSettingsResponse;
import com.hotelbooking.hotel_booking.entity.SignupOtpConfiguration;
import com.hotelbooking.hotel_booking.repository.SignupOtpConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Reads and changes the signup OTP validity row. Kept separate from
 * {@link SignupOtpConfigurationService} so that the call to its
 * {@code evictOtpValidity()} goes through the Spring cache proxy; a call from
 * inside that same class would skip the eviction.
 */
@Service
public class SignupOtpSettingsAdminService {
    private static final Logger log = LoggerFactory.getLogger(SignupOtpSettingsAdminService.class);

    private final SignupOtpConfigurationRepository repository;
    private final SignupOtpConfigurationService configurationService;

    public SignupOtpSettingsAdminService(SignupOtpConfigurationRepository repository,
                                         SignupOtpConfigurationService configurationService) {
        this.repository = repository;
        this.configurationService = configurationService;
    }

    /** Current database value; the effective default when the row is missing. */
    public SignupOtpSettingsResponse getSettings() {
        return repository.findById(SignupOtpConfigurationService.CONFIGURATION_ID)
                .map(row -> new SignupOtpSettingsResponse(row.getValidityMinutes(), row.getUpdatedAt()))
                .orElseGet(() -> new SignupOtpSettingsResponse(
                        Math.toIntExact(configurationService.getOtpValidity().toMinutes()), null));
    }

    /**
     * Saves the new validity (creating the row if needed), then clears the
     * cached value so the next signup uses it immediately. Not
     * {@code @Transactional}: the save commits first, so the eviction can never
     * run before the new value is visible to the next read.
     */
    public SignupOtpSettingsResponse updateValidityMinutes(int validityMinutes) {
        SignupOtpConfiguration row = repository.findById(SignupOtpConfigurationService.CONFIGURATION_ID)
                .orElseGet(() -> new SignupOtpConfiguration(SignupOtpConfigurationService.CONFIGURATION_ID));
        row.updateValidityMinutes(validityMinutes, LocalDateTime.now());
        SignupOtpConfiguration saved = repository.save(row);

        configurationService.evictOtpValidity();
        log.info("Signup OTP validity changed to {} minutes", validityMinutes);
        return new SignupOtpSettingsResponse(saved.getValidityMinutes(), saved.getUpdatedAt());
    }
}
