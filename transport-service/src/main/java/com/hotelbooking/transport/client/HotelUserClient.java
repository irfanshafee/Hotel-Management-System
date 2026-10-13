package com.hotelbooking.transport.client;

import com.hotelbooking.transport.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HotelUserClient {
    private static final ParameterizedTypeReference<HotelApiResponse<HotelUserResponse>>
            USER_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
            };

    private final RestClient hotelServiceRestClient;

    public HotelUserClient(RestClient hotelServiceRestClient) {
        this.hotelServiceRestClient = hotelServiceRestClient;
    }

    public VerifiedUser getCurrentUser(String authorizationHeader) {
        try {
            HotelApiResponse<HotelUserResponse> response = hotelServiceRestClient.get()
                    .uri("/api/user/me")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(USER_RESPONSE_TYPE);

            if (response == null || response.data() == null
                    || response.data().id() == null) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Hotel Service returned an invalid user response");
            }

            HotelUserResponse user = response.data();
            return new VerifiedUser(
                    user.id(), user.name(), user.email(), user.role());
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == HttpStatus.UNAUTHORIZED.value()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED,
                        "Authentication required");
            }
            if (status == HttpStatus.FORBIDDEN.value()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
            }
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Hotel Service is unavailable");
        } catch (ResourceAccessException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Hotel Service is unavailable");
        }
    }
}
