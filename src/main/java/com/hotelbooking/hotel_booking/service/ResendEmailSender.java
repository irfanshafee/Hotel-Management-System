package com.hotelbooking.hotel_booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Shared, server-side Resend transport for transactional messages. */
@Service
@ConditionalOnProperty(name = "app.password-reset.delivery", havingValue = "resend")
class ResendEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResendEmailSender.class);
    private final String apiKey;
    private final String mailFrom;
    private final HttpClient client = HttpClient.newHttpClient();

    ResendEmailSender(@Value("${app.password-reset.resend-api-key}") String apiKey,
                      @Value("${app.password-reset.mail-from}") String mailFrom) {
        this.apiKey = apiKey;
        this.mailFrom = mailFrom;
    }

    void send(String recipientEmail, String subject, String html, String text) {
        if (apiKey.isBlank() || mailFrom.isBlank()) {
            throw new IllegalStateException("Transactional email is not configured");
        }
        String body = "{\"from\":\"" + escape(mailFrom) + "\",\"to\":[\""
                + escape(recipientEmail) + "\"],\"subject\":\"" + escape(subject)
                + "\",\"html\":\"" + escape(html) + "\",\"text\":\""
                + escape(text) + "\"}";
        try {
            HttpResponse<Void> response = client.send(HttpRequest.newBuilder(
                            URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.error("Resend transactional email delivery failed with HTTP status {}", response.statusCode());
                throw new IllegalStateException("Transactional email delivery failed");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Transactional email delivery failed", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Transactional email delivery failed", exception);
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}
