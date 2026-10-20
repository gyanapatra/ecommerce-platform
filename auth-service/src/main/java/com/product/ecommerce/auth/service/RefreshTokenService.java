package com.product.ecommerce.auth.service;

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

import com.product.ecommerce.auth.token.RefreshToken;
import com.product.ecommerce.auth.token.RefreshTokenRepository;
import com.product.ecommerce.auth.user.User;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final Duration refreshTokenTtl;
	private final SecureRandom secureRandom = new SecureRandom();

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
			@Value("${app.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {

		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshTokenTtl = refreshTokenTtl;
	}

	@Transactional
	public String createRefreshToken(User user) {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);

		String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

		String tokenHash = hashToken(rawToken);

		RefreshToken refreshToken = new RefreshToken(user, tokenHash, UUID.randomUUID(),
				Instant.now().plus(refreshTokenTtl));

		refreshTokenRepository.save(refreshToken);

		return rawToken;
	}

	public String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

			return Base64.getEncoder().withoutPadding().encodeToString(hash);

		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}
}
