package com.hotelbooking.hotel_booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "signup_otp_configuration")
public class SignupOtpConfiguration {
    @Id
    private Short id;

    @Column(name = "validity_minutes", nullable = false)
    private Integer validityMinutes;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected SignupOtpConfiguration() {
    }

    public SignupOtpConfiguration(Short id) {
        this.id = id;
    }

    public void updateValidityMinutes(int validityMinutes, LocalDateTime updatedAt) {
        this.validityMinutes = validityMinutes;
        this.updatedAt = updatedAt;
    }

    public Integer getValidityMinutes() {
        return validityMinutes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
