package com.hotelbooking.hotel_booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** One row of "Production".configuration_table: a setting identified by its key. */
@Entity
@Table(name = "configuration_table")
public class ConfigurationEntry {
    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ConfigurationEntry() {
    }

    public ConfigurationEntry(String configKey, String description) {
        this.configKey = configKey;
        this.description = description;
    }

    public void updateValue(String configValue, LocalDateTime updatedAt) {
        this.configValue = configValue;
        this.updatedAt = updatedAt;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
