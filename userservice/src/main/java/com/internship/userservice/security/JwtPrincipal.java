package com.internship.userservice.security;

import com.internship.userservice.model.Role;

public record JwtPrincipal(
        Long userId,
        String login,
        Role role
) {
}