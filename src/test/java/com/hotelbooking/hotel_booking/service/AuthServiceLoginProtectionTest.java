package com.hotelbooking.hotel_booking.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hotelbooking.hotel_booking.dto.AuthResponse;
import com.hotelbooking.hotel_booking.dto.LoginRequest;
import com.hotelbooking.hotel_booking.dto.RegisterRequest;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.enums.UserRole;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import com.hotelbooking.hotel_booking.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceLoginProtectionTest {
    private UserRepository userRepository;
    private JwtService jwtService;
    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, jwtService);
        user = userWithPassword("CorrectPassword1");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
    }

    @Test
    void correctPasswordReturnsJwtAndResetsLoginProtection() {
        user.setFailedLoginAttempts(3);
        AuthResponse response = authService.login(login("CorrectPassword1"));

        assertEquals("jwt-token", response.token());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void oneWrongPasswordPersistsOneFailedAttempt() {
        assertInvalidCredentials(() -> authService.login(login("WrongPassword")));

        assertEquals(1, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void fourthWrongPasswordIsStillGenericUnauthorized() {
        user.setFailedLoginAttempts(3);

        assertInvalidCredentials(() -> authService.login(login("WrongPassword")));

        assertEquals(4, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void fifthWrongPasswordLocksAccountAndPersistsState() {
        user.setFailedLoginAttempts(4);
        LocalDateTime beforeAttempt = LocalDateTime.now();

        ApiException exception = assertThrows(ApiException.class,
                () -> authService.login(login("WrongPassword")));

        assertEquals("account.locked", exception.getMessageKey());
        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.getLockedUntil().isAfter(beforeAttempt.plusMinutes(14)));
        assertTrue(user.getLockedUntil().isBefore(beforeAttempt.plusMinutes(16)));
        verify(userRepository).save(user);
    }

    @Test
    void correctPasswordWhileLockedDoesNotVerifyOrExtendLock() {
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(10);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(lockedUntil);

        assertLocked(() -> authService.login(login("CorrectPassword1")));

        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(lockedUntil, user.getLockedUntil());
        verify(jwtService, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void wrongPasswordWhileLockedDoesNotExtendLock() {
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(10);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(lockedUntil);

        assertLocked(() -> authService.login(login("WrongPassword")));

        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(lockedUntil, user.getLockedUntil());
        verify(userRepository, never()).save(any());
    }

    @Test
    void expiredLockWithCorrectPasswordSucceedsAndClearsState() {
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().minusSeconds(1));

        AuthResponse response = authService.login(login("CorrectPassword1"));

        assertEquals("jwt-token", response.token());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void expiredLockWithWrongPasswordStartsAgainAtOne() {
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().minusSeconds(1));

        assertInvalidCredentials(() -> authService.login(login("WrongPassword")));

        assertEquals(1, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void successfulLoginAfterThreeFailuresResetsCounter() {
        user.setFailedLoginAttempts(3);

        AuthResponse response = authService.login(login("CorrectPassword1"));

        assertEquals("jwt-token", response.token());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void unknownEmailUsesTheSameGenericInvalidCredentialsError() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertInvalidCredentials(() -> authService.login(new LoginRequest(
                "unknown@example.com", "WrongPassword")));
    }

    @Test
    void registrationInitializesLoginProtectionFields() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(new RegisterRequest("New User", 25,
                "new@example.com", "CorrectPassword1"));

        var savedUser = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals(0, savedUser.getValue().getFailedLoginAttempts());
        assertNull(savedUser.getValue().getLockedUntil());
    }

    private void assertInvalidCredentials(Runnable operation) {
        ApiException exception = assertThrows(ApiException.class, operation::run);
        assertEquals("invalid.credentials", exception.getMessageKey());
    }

    private void assertLocked(Runnable operation) {
        ApiException exception = assertThrows(ApiException.class, operation::run);
        assertEquals("account.locked", exception.getMessageKey());
    }

    private LoginRequest login(String password) {
        return new LoginRequest("user@example.com", password);
    }

    private User userWithPassword(String password) {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        testUser.setPassword(BCrypt.withDefaults().hashToString(4, password.toCharArray()));
        testUser.setRole(UserRole.USER);
        testUser.setFailedLoginAttempts(0);
        return testUser;
    }
}
