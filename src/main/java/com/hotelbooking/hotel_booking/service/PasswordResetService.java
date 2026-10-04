package com.hotelbooking.hotel_booking.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hotelbooking.hotel_booking.dto.ForgotPasswordRequest;
import com.hotelbooking.hotel_booking.dto.ResetPasswordRequest;
import com.hotelbooking.hotel_booking.entity.PasswordResetToken;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.PasswordResetTokenRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

@Service
public class PasswordResetService {
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_EXPIRATION_MINUTES = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetTokenDelivery tokenDelivery;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordResetTokenDelivery tokenDelivery) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenDelivery = tokenDelivery;
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email())).orElse(null);
        if (user == null) return;

        LocalDateTime now = LocalDateTime.now();
        tokenRepository.invalidateUnusedTokensForUser(user.getId(), now);
        String rawToken = generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(now.plusMinutes(TOKEN_EXPIRATION_MINUTES));
        tokenRepository.save(token);
        tokenDelivery.deliver(user, rawToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken token = tokenRepository.findByTokenHash(hashToken(request.token()))
                .orElseThrow(() -> new ApiException("password-reset.token.invalid"));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw new ApiException("password-reset.token.invalid");
        }

        User user = token.getUser();
        user.setPassword(BCrypt.withDefaults().hashToString(12, request.newPassword().toCharArray()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        token.setUsedAt(now);
        userRepository.save(user);
        tokenRepository.save(token);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
