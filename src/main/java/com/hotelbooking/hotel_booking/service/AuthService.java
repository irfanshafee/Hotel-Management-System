package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.dto.AuthResponse;
import com.hotelbooking.hotel_booking.dto.LoginRequest;
import com.hotelbooking.hotel_booking.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.dto.CurrentUserResponse;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.UserRole;
import com.hotelbooking.hotel_booking.exception.DuplicateEmailException;
import com.hotelbooking.hotel_booking.exception.InvalidCredentialsException;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import com.hotelbooking.hotel_booking.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setAge(request.age());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        return toAuthResponse(user);
    }

    public CurrentUserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(InvalidCredentialsException::new);
        return new CurrentUserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole());
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
