package com.hotelbooking.transport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.OffsetDateTime;

@Entity
@Table(name = "seat", schema = "transport")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Seat() {
    }

    public Seat(Vehicle vehicle, String seatNumber) {
        this.vehicle = vehicle;
        this.seatNumber = seatNumber;
    }

    @PrePersist
    void prepareForInsert() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}
