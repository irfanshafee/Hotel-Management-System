package com.hotelbooking.hotel_booking.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "signup_verifications") @Getter @Setter @NoArgsConstructor
public class SignupVerification {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true) private String email;
 @Column(nullable=false) private String name;
 private Integer age;
 @Column(name="password_hash", nullable=false) private String passwordHash;
 @Column(name="otp_hash", nullable=false) private String otpHash;
 @Column(name="expires_at", nullable=false) private LocalDateTime expiresAt;
 @Column(name="attempt_count", nullable=false) private Integer attemptCount=0;
 @Column(name="resend_available_at", nullable=false) private LocalDateTime resendAvailableAt;
 @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;
 @PrePersist void timestamps(){ if(createdAt==null) createdAt=LocalDateTime.now(); if(attemptCount==null) attemptCount=0; }
}
