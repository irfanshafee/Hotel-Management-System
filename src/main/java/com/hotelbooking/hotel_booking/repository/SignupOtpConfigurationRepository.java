package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.SignupOtpConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupOtpConfigurationRepository
        extends JpaRepository<SignupOtpConfiguration, Short> {
}
