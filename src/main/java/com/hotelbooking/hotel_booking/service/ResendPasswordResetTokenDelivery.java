package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "app.password-reset.delivery", havingValue = "resend")
class ResendPasswordResetTokenDelivery implements PasswordResetTokenDelivery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResendPasswordResetTokenDelivery.class);
    private final String apiKey, mailFrom, frontendBaseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    ResendPasswordResetTokenDelivery(@Value("${app.password-reset.resend-api-key}") String apiKey,
                                     @Value("${app.password-reset.mail-from}") String mailFrom,
                                     @Value("${app.password-reset.frontend-base-url}") String frontendBaseUrl) {
        this.apiKey = apiKey; this.mailFrom = mailFrom; this.frontendBaseUrl = frontendBaseUrl.replaceAll("/$", "");
    }
    @Override public void deliver(User user, String rawToken) {
        if (apiKey.isBlank() || mailFrom.isBlank()) throw new IllegalStateException("Password reset email is not configured");
        String url = frontendBaseUrl + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String html = "<p>Hello,</p><p>We received a request to reset your Hotel Booking account password.</p>" +
                "<p><a href=\"" + url + "\">Reset Password</a></p><p>This link expires in 30 minutes.</p>" +
                "<p>If you did not request this, you can safely ignore this email.</p>";
        String body = "{\"from\":\"" + escape(mailFrom) + "\",\"to\":[\"" + escape(user.getEmail()) + "\"]," +
                "\"subject\":\"Reset your Hotel Booking password\",\"html\":\"" + escape(html) + "\"}";
        try {
            HttpResponse<Void> response = client.send(HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.error("Resend password reset delivery failed with HTTP status {}", response.statusCode());
                throw new IllegalStateException("Password reset email delivery failed");
            }
        } catch (Exception exception) { throw new IllegalStateException("Password reset email delivery failed", exception); }
    }
    private String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
