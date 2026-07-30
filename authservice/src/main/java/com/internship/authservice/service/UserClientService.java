package com.internship.authservice.service;

import com.internship.authservice.dto.CreateUserRequest;

public interface UserClientService {
    Long createUser(CreateUserRequest request);

    void deleteUser(Long userId);
}
