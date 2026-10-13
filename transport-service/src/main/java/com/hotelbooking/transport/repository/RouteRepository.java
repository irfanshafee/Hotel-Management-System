package com.hotelbooking.transport.repository;

import com.hotelbooking.transport.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
