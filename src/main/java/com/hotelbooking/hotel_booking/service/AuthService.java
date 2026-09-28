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

import java.util.Locale;

@Service
public class AuthService {
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

        User savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("invalid.credentials"));
        BCrypt.Result verification = BCrypt.verifyer().verify(
                request.password().toCharArray(), user.getPassword());
        if (!verification.verified) {
            throw new ApiException("invalid.credentials");
        }
        return toAuthResponse(user);
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
