package com.internship.authservice.service;

import java.time.Instant;

public interface RefreshTokenService {
    String generateRefreshTokenValue();
    String hashToken(String token);
    Instant resolveRefreshTokenExpiry(Boolean rememberMe);
}
