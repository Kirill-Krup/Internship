package com.internship.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenValidationRequest(
        @NotBlank(message = "Token can not be empty")
        String token
) {
}