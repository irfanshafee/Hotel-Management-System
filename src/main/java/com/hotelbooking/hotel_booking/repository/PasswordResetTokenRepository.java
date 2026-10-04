package com.hotelbooking.hotel_booking.repository;

import com.hotelbooking.hotel_booking.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken token set token.usedAt = :invalidatedAt " +
            "where token.user.id = :userId and token.usedAt is null")
    int invalidateUnusedTokensForUser(Long userId, LocalDateTime invalidatedAt);
}
