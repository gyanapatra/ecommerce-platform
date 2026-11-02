package com.product.ecommerce.gateway.filter;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.product.ecommerce.gateway.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class GatewayJwtAuthenticationManager implements ReactiveAuthenticationManager {

	private final SecretKey signingKey;

	public GatewayJwtAuthenticationManager(JwtProperties jwtProperties) {

		byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());

		if (keyBytes.length < 32) {
			throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
		}

		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {

		String token = authentication.getCredentials().toString();

		try {
			Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();

			String userId = claims.getSubject();
			String role = claims.get("role", String.class);

			if (userId == null || userId.isBlank() || role == null || role.isBlank() || claims.getIssuedAt() == null
					|| claims.getExpiration() == null || claims.getIssuedAt().after(new Date())) {

				return Mono.error(new BadCredentialsException("Invalid JWT"));
			}

			String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

			Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(userId, null,
					java.util.List.of(new SimpleGrantedAuthority(authority)));

			return Mono.just(authenticated);

		} catch (RuntimeException ex) {
			return Mono.error(new BadCredentialsException("Invalid JWT", ex));
		}
	}
}