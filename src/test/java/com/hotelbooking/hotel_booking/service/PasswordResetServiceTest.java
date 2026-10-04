package com.hotelbooking.hotel_booking.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hotelbooking.hotel_booking.dto.ForgotPasswordRequest;
import com.hotelbooking.hotel_booking.dto.ResetPasswordRequest;
import com.hotelbooking.hotel_booking.entity.PasswordResetToken;
import com.hotelbooking.hotel_booking.entity.User;
import com.hotelbooking.hotel_booking.exception.ApiException;
import com.hotelbooking.hotel_booking.repository.PasswordResetTokenRepository;
import com.hotelbooking.hotel_booking.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    @Test
    void requestForExistingUserStoresHashAndDeliversOnlyRawToken() {
        User user = user();
        UserRepository users = mock(UserRepository.class);
        PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
        PasswordResetTokenDelivery delivery = mock(PasswordResetTokenDelivery.class);
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        PasswordResetService service = new PasswordResetService(users, tokens, delivery);

        service.requestReset(new ForgotPasswordRequest("USER@example.com"));

        var captured = org.mockito.ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokens).save(captured.capture());
        PasswordResetToken token = captured.getValue();
        assertEquals(user, token.getUser());
        assertEquals(64, token.getTokenHash().length());
        assertNull(token.getUsedAt());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(29)));
        verify(delivery).deliver(eq(user), argThat(raw -> !raw.equals(token.getTokenHash())));
    }

    @Test
    void unknownEmailCreatesNoTokenAndHasNoDelivery() {
        UserRepository users = mock(UserRepository.class);
        PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
        PasswordResetTokenDelivery delivery = mock(PasswordResetTokenDelivery.class);
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        new PasswordResetService(users, tokens, delivery)
                .requestReset(new ForgotPasswordRequest("unknown@example.com"));
        verifyNoInteractions(tokens, delivery);
    }

    @Test
    void hashingIsDeterministicAndDoesNotStoreRawToken() {
        String hash = PasswordResetService.hashToken("raw-token");
        assertEquals(hash, PasswordResetService.hashToken("raw-token"));
        assertNotEquals("raw-token", hash);
        assertEquals(64, hash.length());
    }

    @Test
    void validResetChangesPasswordMarksTokenUsedAndClearsLock() {
        User user = user(); user.setFailedLoginAttempts(5); user.setLockedUntil(LocalDateTime.now().plusMinutes(1));
        PasswordResetToken token = token(user, "raw-token", LocalDateTime.now().plusMinutes(30));
        UserRepository users = mock(UserRepository.class);
        PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
        when(tokens.findByTokenHash(PasswordResetService.hashToken("raw-token"))).thenReturn(Optional.of(token));
        PasswordResetService service = new PasswordResetService(users, tokens, mock(PasswordResetTokenDelivery.class));

        service.resetPassword(new ResetPasswordRequest("raw-token", "NewPassword1"));

        assertTrue(BCrypt.verifyer().verify("NewPassword1".toCharArray(), user.getPassword()).verified);
        assertEquals(0, user.getFailedLoginAttempts()); assertNull(user.getLockedUntil()); assertNotNull(token.getUsedAt());
    }

    @Test
    void usedOrExpiredTokenIsRejected() {
        User user = user(); PasswordResetToken token = token(user, "raw-token", LocalDateTime.now().minusSeconds(1));
        PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        PasswordResetService service = new PasswordResetService(mock(UserRepository.class), tokens, mock(PasswordResetTokenDelivery.class));
        ApiException error = assertThrows(ApiException.class, () -> service.resetPassword(new ResetPasswordRequest("raw-token", "NewPassword1")));
        assertEquals("password-reset.token.invalid", error.getMessageKey());
    }

    private User user() { User user = new User(); user.setId(1L); user.setEmail("user@example.com"); user.setPassword(BCrypt.withDefaults().hashToString(4, "OldPassword1".toCharArray())); return user; }
    private PasswordResetToken token(User user, String raw, LocalDateTime expires) { PasswordResetToken token = new PasswordResetToken(); token.setUser(user); token.setTokenHash(PasswordResetService.hashToken(raw)); token.setExpiresAt(expires); return token; }
}
