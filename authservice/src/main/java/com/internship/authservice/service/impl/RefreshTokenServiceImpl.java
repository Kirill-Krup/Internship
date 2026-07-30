package com.internship.authservice.service.impl;

import com.internship.authservice.service.RefreshTokenService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    public Instant resolveRefreshTokenExpiry(Boolean rememberMe) {
        if (Boolean.TRUE.equals(rememberMe)) {
            return Instant.now().plusSeconds(7L * 24 * 60 * 60);
        }
        return Instant.now().plusSeconds(24L * 60 * 60);
    }

    public String generateRefreshTokenValue() {
        byte[] bytes = new byte[64];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }
    }
}
