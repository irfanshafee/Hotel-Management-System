package com.hotelbooking.transport.entity;

import com.hotelbooking.transport.enums.DriverStatus;
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
@Table(name = "driver", schema = "transport")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "nid_number", nullable = false, unique = true)
    private String nidNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Driver() {
    }

    public Driver(String name, String phoneNumber, String nidNumber, DriverStatus status) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.nidNumber = nidNumber;
        this.status = status;
    }

    public void update(String name, String phoneNumber, String nidNumber, DriverStatus status) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.nidNumber = nidNumber;
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

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNidNumber() {
        return nidNumber;
    }

    public DriverStatus getStatus() {
        return status;
    }
}
