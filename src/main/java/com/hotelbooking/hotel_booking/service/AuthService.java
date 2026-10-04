package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.AuthResponse;
import com.hotelbooking.hotel_booking.dto.LoginRequest;
import com.hotelbooking.hotel_booking.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.UserRole;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import com.hotelbooking.hotel_booking.security.JwtService;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 1;

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException("duplicate.email");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setAge(request.age());
        user.setEmail(email);
        user.setPassword(BCrypt.withDefaults().hashToString(12, request.password().toCharArray()));
        user.setRole(UserRole.USER);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        User savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("invalid.credentials"));

        LocalDateTime now = LocalDateTime.now();
        if (isCurrentlyLocked(user, now)) {
            throw new ApiException("account.locked");
        }

        boolean expiredLockWasCleared = user.getLockedUntil() != null;
        if (expiredLockWasCleared) {
            resetLoginProtection(user);
        }

        BCrypt.Result verification = BCrypt.verifyer().verify(
                request.password().toCharArray(), user.getPassword());
        if (!verification.verified) {
            int failedAttempts = Math.max(0, user.getFailedLoginAttempts() == null
                    ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setFailedLoginAttempts(MAX_FAILED_LOGIN_ATTEMPTS);
                user.setLockedUntil(now.plusMinutes(LOCK_DURATION_MINUTES));
                userRepository.save(user);
                throw new ApiException("account.locked");
            }

            userRepository.save(user);
            throw new ApiException("invalid.credentials");
        }

        if (expiredLockWasCleared
                || user.getFailedLoginAttempts() == null
                || user.getFailedLoginAttempts() != 0
                || user.getLockedUntil() != null) {
            resetLoginProtection(user);
            userRepository.save(user);
        }
        return toAuthResponse(user);
    }

    private boolean isCurrentlyLocked(User user, LocalDateTime now) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(now);
    }

    private void resetLoginProtection(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user), user.getId(), user.getName(),
                user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
