package com.internship.authservice.dto;

import com.internship.authservice.model.Role;

public record TokenValidationResponse(
        boolean valid,
        Long userId,
        String login,
        Role role
) {
}