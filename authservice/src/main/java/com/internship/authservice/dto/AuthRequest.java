package com.internship.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Login can not be empty")
    @Size(min = 3, max = 50, message = "Login must be at least 3 characters and max 50")
    private String login;

    @NotBlank(message = "Password can not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private boolean rememberMe;
}
