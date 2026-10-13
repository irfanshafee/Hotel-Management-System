package com.hotelbooking.transport.entity;

import com.hotelbooking.transport.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trip", schema = "transport")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "departure_time", nullable = false)
    private OffsetDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private OffsetDateTime arrivalTime;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Trip() {
    }

    public Trip(Vehicle vehicle, Route route, Driver driver,
                OffsetDateTime departureTime, OffsetDateTime arrivalTime,
                BigDecimal fare, TripStatus status) {
        update(vehicle, route, driver, departureTime, arrivalTime, fare, status);
    }

    public void update(Vehicle vehicle, Route route, Driver driver,
                       OffsetDateTime departureTime, OffsetDateTime arrivalTime,
                       BigDecimal fare, TripStatus status) {
        this.vehicle = vehicle;
        this.route = route;
        this.driver = driver;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.fare = fare;
        this.status = status;
    }

    public void cancel() {
        this.status = TripStatus.CANCELLED;
    }

    @PrePersist
    void prepareForInsert() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void prepareForUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Route getRoute() {
        return route;
    }

    public Driver getDriver() {
        return driver;
    }

    public OffsetDateTime getDepartureTime() {
        return departureTime;
    }

    public OffsetDateTime getArrivalTime() {
        return arrivalTime;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public TripStatus getStatus() {
        return status;
    }
}
