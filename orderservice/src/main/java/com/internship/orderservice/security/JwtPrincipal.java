package com.internship.orderservice.security;

import com.internship.orderservice.model.Role;

public record JwtPrincipal(
    Long userId,
    String login,
    Role role
) {
}
