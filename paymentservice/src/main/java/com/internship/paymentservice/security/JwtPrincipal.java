package com.internship.paymentservice.security;

import com.internship.paymentservice.model.Role;

public record JwtPrincipal(
    Long userId,
    String login,
    Role role
) {
}
