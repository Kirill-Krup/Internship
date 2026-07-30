package com.internship.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token can not be empty")
        String token
) {
}