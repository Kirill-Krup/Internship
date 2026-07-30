package com.internship.authservice.service.impl;

import com.internship.authservice.config.properties.UserServiceProperties;
import com.internship.authservice.dto.CreateUserRequest;
import com.internship.authservice.dto.CreatedUserResponse;
import com.internship.authservice.service.UserClientService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class UserClientServiceImpl implements UserClientService {

    private final RestTemplate restTemplate;
    private final UserServiceProperties userServiceProperties;

    @Override
    public Long createUser(CreateUserRequest request) {
        String url = buildUrl(
                userServiceProperties.getBaseUrl(),
                userServiceProperties.getCreatePath()
        );

        ResponseEntity<CreatedUserResponse> response =
                restTemplate.postForEntity(url, request, CreatedUserResponse.class);

        CreatedUserResponse body = response.getBody();

        if (body == null || body.id() == null) {
            throw new IllegalStateException("User service returned empty response");
        }

        return body.id();
    }

    @Override
    public void deleteUser(Long userId) {
        String path = userServiceProperties.getDeletePath().replace("{id}", String.valueOf(userId));
        String url = buildUrl(userServiceProperties.getBaseUrl(), path);

        restTemplate.delete(url);
    }

    private String buildUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }
}