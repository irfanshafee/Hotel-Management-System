package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.ConfigurationResponse;
import com.hotelbooking.hotel_booking.entity.ConfigurationEntry;
import com.hotelbooking.hotel_booking.enums.ConfigurationKey;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.ConfigurationEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Lists and changes rows of configuration_table for the admin API. */
@Service
public class ConfigurationAdminService {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationAdminService.class);

    private final ConfigurationEntryRepository repository;
    private final ConfigurationService configurationService;

    public ConfigurationAdminService(ConfigurationEntryRepository repository,
                                     ConfigurationService configurationService) {
        this.repository = repository;
        this.configurationService = configurationService;
    }

    public List<ConfigurationResponse> getAll() {
        return repository.findAll(Sort.by("configKey")).stream()
                .map(ConfigurationAdminService::toResponse)
                .toList();
    }

    public ConfigurationResponse get(String key) {
        return repository.findById(key)
                .map(ConfigurationAdminService::toResponse)
                .orElseThrow(() -> new ApiException("configuration.not-found", key));
    }

    /**
     * Saves the new value, then clears that key's cached value so the next read
     * uses it immediately. Known keys are validated and created if their row is
     * missing; unknown keys must already exist. Not {@code @Transactional}: the
     * save commits before the eviction, so the next read always sees the new value.
     */
    public ConfigurationResponse update(String key, String value) {
        ConfigurationKey knownKey = ConfigurationKey.fromKey(key).orElse(null);
        String newValue = value.trim();
        if (knownKey != null && knownKey.parse(newValue).isEmpty()) {
            throw new ApiException("configuration.value.invalid", key,
                    String.valueOf(knownKey.minValue()), String.valueOf(knownKey.maxValue()));
        }

        ConfigurationEntry entry = repository.findById(key).orElseGet(() -> {
            if (knownKey == null) {
                throw new ApiException("configuration.not-found", key);
            }
            return new ConfigurationEntry(key, knownKey.description());
        });
        entry.updateValue(newValue, LocalDateTime.now());
        ConfigurationEntry saved = repository.save(entry);

        configurationService.evict(key);
        log.info("Configuration {} changed to {}", key, newValue);
        return toResponse(saved);
    }

    private static ConfigurationResponse toResponse(ConfigurationEntry entry) {
        return new ConfigurationResponse(entry.getConfigKey(), entry.getConfigValue(),
                entry.getDescription(), entry.getUpdatedAt());
    }
}
