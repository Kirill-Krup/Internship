package com.internship.authservice.service.impl;

import com.internship.authservice.dto.AuthRequest;
import com.internship.authservice.dto.JwtResponse;
import com.internship.authservice.dto.RefreshTokenRequest;
import com.internship.authservice.dto.RegisterRequest;
import com.internship.authservice.dto.TokenValidationResponse;
import com.internship.authservice.exception.UserAlreadyExistsException;
import com.internship.authservice.model.RefreshToken;
import com.internship.authservice.model.Role;
import com.internship.authservice.model.UserCredentials;
import com.internship.authservice.repository.RefreshTokenRepository;
import com.internship.authservice.repository.UserCredentialsRepository;
import com.internship.authservice.service.AuthService;
import com.internship.authservice.service.RefreshTokenService;
import com.internship.authservice.service.UserClientService;
import com.internship.authservice.util.JwtUtil;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserClientService userClientService;

    @Override
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userCredentialsRepository.existsByLogin(request.getLogin())) {
            throw new UserAlreadyExistsException(
                    "User with login " + request.getLogin() + " already exists"
            );
        }
        Long userId = userClientService.createUser(request.getUser());
        try {
            UserCredentials credentials = new UserCredentials();
            credentials.setUserId(userId);
            credentials.setLogin(request.getLogin());
            credentials.setHashedPassword(passwordEncoder.encode(request.getPassword()));
            credentials.setRole(Role.USER);
            UserCredentials savedCredentials = userCredentialsRepository.save(credentials);
            String accessToken = jwtUtil.generateAccessToken(
                    savedCredentials.getUserId(),
                    savedCredentials.getLogin(),
                    savedCredentials.getRole()
            );
            String refreshToken = createAndStoreRefreshToken(
                    savedCredentials,
                    request.isRememberMe()
            );
            return new JwtResponse(accessToken, refreshToken);
        } catch (DataAccessException ex) {
            userClientService.deleteUser(userId);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse login(AuthRequest request) {
        UserCredentials credentials = userCredentialsRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));

        if (!passwordEncoder.matches(request.getPassword(), credentials.getHashedPassword())) {
            throw new BadCredentialsException("Invalid login or password");
        }

        String accessToken = jwtUtil.generateAccessToken(
                credentials.getUserId(),
                credentials.getLogin(),
                credentials.getRole()
        );

        String refreshToken = createAndStoreRefreshToken(credentials, request.isRememberMe());

        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public JwtResponse refresh(RefreshTokenRequest request) {
        String rawRefreshToken = request.token();
        String tokenHash = refreshTokenService.hashToken(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BadCredentialsException("Refresh token has expired");
        }

        UserCredentials credentials = storedToken.getCredentials();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtUtil.generateAccessToken(
                credentials.getUserId(),
                credentials.getLogin(),
                credentials.getRole()
        );

        String newRefreshToken = createAndStoreRefreshToken(credentials, true);

        return new JwtResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public TokenValidationResponse validate(String token) {
        boolean valid = jwtUtil.isValid(token);

        if (!valid) {
            return new TokenValidationResponse(false, null, null, null);
        }

        Long userId = jwtUtil.extractUserId(token);
        String login = jwtUtil.extractLogin(token);
        Role role = null;

        if (jwtUtil.isAccessToken(token)) {
            role = jwtUtil.extractRole(token);
        }

        return new TokenValidationResponse(true, userId, login, role);
    }

    private String createAndStoreRefreshToken(UserCredentials credentials, Boolean rememberMe) {
        String rawRefreshToken = refreshTokenService.generateRefreshTokenValue();
        String tokenHash = refreshTokenService.hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setCredentials(credentials);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(refreshTokenService.resolveRefreshTokenExpiry(rememberMe));

        refreshTokenRepository.save(refreshToken);

        return rawRefreshToken;
    }


}