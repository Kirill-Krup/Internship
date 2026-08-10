package com.internship.authservice.security;

import com.internship.authservice.model.Role;

public record JwtPrincipal(
        Long userId,
        String login,
        Role role
) {
}