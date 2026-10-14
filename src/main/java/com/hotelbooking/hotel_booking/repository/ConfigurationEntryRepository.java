package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.ConfigurationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationEntryRepository extends JpaRepository<ConfigurationEntry, String> {
}
