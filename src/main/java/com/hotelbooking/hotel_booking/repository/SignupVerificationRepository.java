package com.hotelbooking.hotel_booking.repository;
import com.hotelbooking.hotel_booking.entity.SignupVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface SignupVerificationRepository extends JpaRepository<SignupVerification,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<SignupVerification> findByEmail(String email);
}
