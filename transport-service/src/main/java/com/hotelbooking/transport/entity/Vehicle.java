package com.hotelbooking.transport.entity;

import com.hotelbooking.transport.enums.VehicleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicle", schema = "transport")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_number", nullable = false, unique = true)
    private String busNumber;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Vehicle() {
    }

    public Vehicle(String busNumber, String name, Integer totalSeats, VehicleStatus status) {
        this.busNumber = busNumber;
        this.name = name;
        this.totalSeats = totalSeats;
        this.status = status;
    }

    public void update(String name, VehicleStatus status) {
        this.name = name;
        this.status = status;
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

    public String getBusNumber() {
        return busNumber;
    }

    public String getName() {
        return name;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public VehicleStatus getStatus() {
        return status;
    }
}
