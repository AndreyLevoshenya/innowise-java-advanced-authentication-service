package com.inno.service.client;

import com.inno.service.exception.UserServiceException;
import com.inno.service.dto.CreateUserRequest;
import com.inno.service.dto.UserDtoResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient userServiceRestClient) {
        this.restClient = userServiceRestClient;
    }

    public UserDtoResponse createUser(CreateUserRequest request, String serviceAccessToken) {
        try {
            return restClient.post()
                    .uri("/api/v1/users")
                    .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                    .body(request)
                    .retrieve()
                    .body(UserDtoResponse.class);
        } catch (RestClientResponseException e) {
            throw new UserServiceException(
                    "Failed to create user. HTTP status: " + e.getStatusCode(),
                    e
            );
        }
    }
}