package com.internship.authservice.service;

import com.internship.authservice.dto.AuthRequest;
import com.internship.authservice.dto.JwtResponse;
import com.internship.authservice.dto.RefreshTokenRequest;
import com.internship.authservice.dto.RegisterRequest;
import com.internship.authservice.dto.TokenValidationResponse;

public interface AuthService {

    JwtResponse register(RegisterRequest request);

    JwtResponse login(AuthRequest request);

    JwtResponse refresh(RefreshTokenRequest request);

    TokenValidationResponse validate(String token);
}