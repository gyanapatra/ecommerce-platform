package com.product.ecommerce.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.product.ecommerce.gateway.config.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.test.StepVerifier;

class GatewayJwtAuthenticationManagerTest {

    private GatewayJwtAuthenticationManager authenticationManager;

    private SecretKey signingKey;

    private static final String USER_ID = "8c4ff334-6559-49ed-b1bb-95e613f7953d";

    private static final String ROLE = "CUSTOMER";

    @BeforeEach
    void setUp() {

        // Generate a 256-bit test key.
        byte[] keyBytes = new byte[32];

        new java.security.SecureRandom().nextBytes(keyBytes);

        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        signingKey = Keys.hmacShaKeyFor(keyBytes);

        JwtProperties jwtProperties = new JwtProperties(base64Secret);

        authenticationManager = new GatewayJwtAuthenticationManager(jwtProperties);
    }

    @Test
    void shouldRejectExpiredJwt() {

        Instant now = Instant.now();

        String expiredToken = Jwts.builder()
                .subject(USER_ID)
                .claim("role", ROLE)
                .issuedAt(Date.from(now.minusSeconds(600)))
                .expiration(Date.from(now.minusSeconds(300)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("test", expiredToken);

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void shouldRejectInvalidJwt() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("test", "invalid.jwt.token");

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void shouldAcceptValidJwt() {

        Instant now = Instant.now();

        String validToken = Jwts.builder()
                .subject(USER_ID)
                .claim("role", ROLE)
                .issuedAt(Date.from(now.minusSeconds(60)))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("test", validToken);

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .assertNext(result -> {

                    assertTrue(result.isAuthenticated());

                    assertEquals(USER_ID, result.getPrincipal());

                    assertTrue(result.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority().equals("ROLE_CUSTOMER")));
                })
                .verifyComplete();
    }
    
    @Test
    void shouldRejectTamperedJwt() {

        Instant now = Instant.now();

        // Generate a legitimate JWT with CUSTOMER role
        String validToken = Jwts.builder()
                .subject(USER_ID)
                .claim("role", "CUSTOMER")
                .issuedAt(Date.from(now.minusSeconds(60)))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        // Split JWT into its three components
        String[] parts = validToken.split("\\.");

        // Decode the JWT payload
        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);

        String payload = new String(
                payloadBytes,
                java.nio.charset.StandardCharsets.UTF_8
        );

        // Modify CUSTOMER role to ADMIN
        String tamperedPayload = payload.replace(
                "\"role\":\"CUSTOMER\"",
                "\"role\":\"ADMIN\""
        );

        // Encode modified payload without changing the original signature
        String modifiedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tamperedPayload.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                ));

        String tamperedToken = parts[0] + "." +
                modifiedPayload + "." +
                parts[2];

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("test", tamperedToken);

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(BadCredentialsException.class)
                .verify();
    }
}
