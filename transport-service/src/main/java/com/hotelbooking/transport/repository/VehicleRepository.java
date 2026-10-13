package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
