package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}
