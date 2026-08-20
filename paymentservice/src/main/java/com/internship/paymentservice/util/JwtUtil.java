package com.internship.paymentservice.util;

import com.internship.paymentservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

  private final String secret;
  private final long accessTokenExpireMillis;

  private Key key;

  public JwtUtil(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration-minutes}") long accessExpirationMinutes
  ) {
    this.secret = secret;
    this.accessTokenExpireMillis = accessExpirationMinutes * 60 * 1000;
  }

  @PostConstruct
  void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public boolean isValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public boolean isAccessToken(String token) {
    return "access".equals(parseClaims(token).get("type", String.class));
  }

  public Long extractUserId(String token) {
    return parseClaims(token).get("userId", Long.class);
  }

  public String extractLogin(String token) {
    return parseClaims(token).getSubject();
  }

  public Role extractRole(String token) {
    String role = parseClaims(token).get("role", String.class);
    return Role.valueOf(role);
  }
}
