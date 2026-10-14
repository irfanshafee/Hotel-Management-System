package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.config.CachingConfiguration;
import com.hotelbooking.hotel_booking.entity.ConfigurationEntry;
import com.hotelbooking.hotel_booking.repository.ConfigurationEntryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cached read access to "Production".configuration_table, one cache entry per
 * key (Redis key "hotelbooking:appSettings::&lt;config_key&gt;").
 * <p>
 * Callers must be other beans: cache annotations work through a Spring proxy,
 * so a call from inside this class would bypass the cache.
 */
@Service
public class ConfigurationService {
    private final ConfigurationEntryRepository repository;

    public ConfigurationService(ConfigurationEntryRepository repository) {
        this.repository = repository;
    }

    /**
     * The stored value for {@code key}, or null when the row is missing. Null is
     * never cached, so a later read retries.
     */
    @Cacheable(cacheNames = CachingConfiguration.APP_SETTINGS_CACHE, key = "#key",
            unless = "#result == null")
    public String getValue(String key) {
        return repository.findById(key)
                .map(ConfigurationEntry::getConfigValue)
                .orElse(null);
    }

    /** Drops the cached value for {@code key} so the next read hits the database. */
    @CacheEvict(cacheNames = CachingConfiguration.APP_SETTINGS_CACHE, key = "#key")
    public void evict(String key) {
    }
}
