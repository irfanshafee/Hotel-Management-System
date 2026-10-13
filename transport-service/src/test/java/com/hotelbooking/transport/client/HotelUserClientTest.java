package com.hotelbooking.transport.client;

import com.hotelbooking.transport.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HotelUserClientTest {
    private MockRestServiceServer server;
    private HotelUserClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://hotel-service");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new HotelUserClient(builder.build());
    }

    @Test
    void forwardsBearerTokenAndMapsVerifiedUser() {
        server.expect(once(), requestTo("http://hotel-service/api/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andRespond(withSuccess("""
                        {
                          "responseCode": 200,
                          "responseMessage": "User retrieved",
                          "data": {
                            "id": 25,
                            "name": "Test User",
                            "email": "user@example.com",
                            "role": "USER"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        VerifiedUser user = client.getCurrentUser("Bearer valid-token");

        assertEquals(25L, user.id());
        assertEquals("user@example.com", user.email());
        assertEquals("USER", user.role());
        server.verify();
    }

    @Test
    void hotelUnauthorizedBecomesTransportUnauthorized() {
        server.expect(once(), requestTo("http://hotel-service/api/user/me"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        ApiException exception = assertThrows(ApiException.class,
                () -> client.getCurrentUser("Bearer invalid-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        server.verify();
    }

    @Test
    void hotelServerFailureBecomesServiceUnavailable() {
        server.expect(once(), requestTo("http://hotel-service/api/user/me"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ApiException exception = assertThrows(ApiException.class,
                () -> client.getCurrentUser("Bearer valid-token"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        server.verify();
    }
}
