package com.product.ecommerce.auth.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.ecommerce.auth.exception.InvalidCredentialsException;
import com.product.ecommerce.auth.security.JwtService;
import com.product.ecommerce.auth.user.User;
import com.product.ecommerce.auth.user.UserRepository;
import com.product.ecommerce.auth.user.dto.LoginRequest;
import com.product.ecommerce.auth.user.dto.LoginResponse;
import com.product.ecommerce.auth.user.dto.RegisterRequest;
import com.product.ecommerce.auth.user.dto.RegisterResponse;

import jakarta.validation.Valid;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public RegisterResponse register(@Valid RegisterRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        String passwordHash = passwordEncoder.encode(
                request.password()
        );

        User user = new User(
                normalizedEmail,
                passwordHash
        );

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setUpdatedAt(Instant.now());

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getCreatedAt()
            );

        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "An account with this email already exists",
                    exception
            );
        }
    }
    
    @Transactional
    public LoginResponse login(LoginRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenTtlSeconds(),
                user.getId(),
                user.getEmail()
        );
    }
}