package com.product.ecommerce.auth.service;

import com.product.ecommerce.auth.exception.InvalidCredentialsException;
import com.product.ecommerce.auth.token.RefreshToken;
import com.product.ecommerce.auth.token.RefreshTokenRepository;
import com.product.ecommerce.auth.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public String createRefreshToken(User user) {

        UUID familyId = UUID.randomUUID();

        return createRefreshToken(user, familyId);
    }
    
    private String createRefreshToken(User user, UUID familyId) {

        String rawToken = generateRawToken();

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                familyId,
                Instant.now().plus(refreshTokenTtl));

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public RotatedRefreshToken rotateRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken existingToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(InvalidCredentialsException::new);

        // Reuse detection: a previously rotated token is being reused.
        if (existingToken.isRevoked()) {

            revokeTokenFamily(existingToken.getFamilyId());

            throw new InvalidCredentialsException();
        }

        if (existingToken.isExpired()) {
            existingToken.revoke();
            throw new InvalidCredentialsException();
        }

        User user = existingToken.getUser();

        if (!user.isEnabled()) {
            revokeTokenFamily(existingToken.getFamilyId());
            throw new InvalidCredentialsException();
        }

        String newRawToken = generateRawToken();

        RefreshToken replacementToken = new RefreshToken(
                user,
                hashToken(newRawToken),
                existingToken.getFamilyId(),
                Instant.now().plus(refreshTokenTtl));

        // Flush first so the replacement token receives its database ID.
        RefreshToken savedReplacement =
                refreshTokenRepository.saveAndFlush(replacementToken);

        existingToken.revoke(savedReplacement.getId());

        return new RotatedRefreshToken(user, newRawToken);
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .ifPresent(token -> revokeTokenFamily(token.getFamilyId()));
    }

    private void revokeTokenFamily(UUID familyId) {

        refreshTokenRepository.findAllByFamilyIdForUpdate(familyId)
                .forEach(RefreshToken::revoke);
    }

    private String generateRawToken() {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable", ex);
        }
    }

    public record RotatedRefreshToken(
            User user,
            String refreshToken) {
    }
}