package com.product.ecommerce.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.product.ecommerce.auth.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JwtServiceTest {

private static final String JWT_SECRET = Base64.getEncoder()
        .encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes());

private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

private JwtService jwtService;
private User user;

@BeforeEach
void setUp() {
    jwtService = new JwtService(JWT_SECRET, ACCESS_TOKEN_TTL);
    user = new User("customer@example.com", "hashed-password");
}

@Test
void shouldGenerateValidAccessToken() {
    String token = jwtService.generateAccessToken(user);

    assertNotNull(token);
    assertFalse(token.isBlank());
    assertTrue(jwtService.isTokenValid(token));
}

@Test
void shouldExtractUserClaimsFromAccessToken() {
    String token = jwtService.generateAccessToken(user);

    Claims claims = jwtService.extractAllClaims(token);

    assertEquals(user.getId().toString(), claims.getSubject());
    assertEquals("customer@example.com", claims.get("email"));
    assertEquals("CUSTOMER", claims.get("role"));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
}

@Test
void shouldExtractUserIdFromAccessToken() {
    String token = jwtService.generateAccessToken(user);

    UUID extractedUserId = jwtService.extractUserId(token);

    assertEquals(user.getId(), extractedUserId);
}

@Test
void shouldReturnConfiguredAccessTokenTtlInSeconds() {
    assertEquals(900L, jwtService.getAccessTokenTtlSeconds());
}

@Test
void shouldRejectMalformedToken() {
    assertFalse(jwtService.isTokenValid("not-a-valid-jwt"));
}

@Test
void shouldRejectTokenSignedWithDifferentSecret() {
    String token = jwtService.generateAccessToken(user);

    String differentSecret = Base64.getEncoder()
            .encodeToString(
                    "abcdef0123456789abcdef0123456789".getBytes());

    JwtService differentJwtService = new JwtService(
            differentSecret, ACCESS_TOKEN_TTL);

    assertFalse(differentJwtService.isTokenValid(token));
}

@Test
void shouldRejectExpiredToken() {
    Duration expiredTtl = Duration.ofSeconds(-1);

    JwtService expiredJwtService = new JwtService(
            JWT_SECRET, expiredTtl);

    String expiredToken = expiredJwtService.generateAccessToken(user);

    assertFalse(expiredJwtService.isTokenValid(expiredToken));
}

@Test
void shouldRejectSecretShorterThan32Bytes() {
    String shortSecret = Base64.getEncoder()
            .encodeToString("short-secret".getBytes());

    assertThrows(
            IllegalArgumentException.class,
            () -> new JwtService(shortSecret, ACCESS_TOKEN_TTL));
}

@Test
void shouldRejectTokenWithInvalidSignatureWhenExtractingClaims() {
    String token = jwtService.generateAccessToken(user);

    String differentSecret = Base64.getEncoder()
            .encodeToString(
                    "abcdef0123456789abcdef0123456789".getBytes());

    JwtService differentJwtService = new JwtService(
            differentSecret, ACCESS_TOKEN_TTL);

    assertThrows(
            io.jsonwebtoken.JwtException.class,
            () -> differentJwtService.extractAllClaims(token));
}

@Test
void shouldRejectTokenWithMissingSubject() {
    String tokenWithoutSubject = Jwts.builder()
            .claim("email", user.getEmail())
            .claim("role", user.getRole())
            .issuedAt(new java.util.Date())
            .expiration(
                    java.util.Date.from(
                            java.time.Instant.now().plusSeconds(300)))
            .signWith(
                    io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                            Base64.getDecoder().decode(JWT_SECRET)),
                    Jwts.SIG.HS256)
            .compact();

    assertFalse(jwtService.isTokenValid(tokenWithoutSubject));
}

}
