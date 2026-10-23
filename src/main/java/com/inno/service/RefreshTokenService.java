package com.inno.service;

import com.inno.repository.RefreshTokenRepository;
import com.inno.repository.model.RefreshToken;
import com.inno.service.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import static com.inno.service.exception.ExceptionErrorCodes.AUTHENTICATION_FAILED;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom;
    private final Duration ttl;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-ttl}") Duration ttl
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureRandom = new SecureRandom();
        this.ttl = ttl;
    }

    @Transactional
    public String create(Long userId) {
        String rawToken = generateToken();

        RefreshToken refreshToken = new RefreshToken(
                userId,
                hashToken(rawToken),
                LocalDateTime.now().plus(ttl)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional(readOnly = true)
    public Long validateAndGetUserId(String rawToken) {
        RefreshToken refreshToken = findValidToken(rawToken);

        return refreshToken.getUserId();
    }

    @Transactional
    public String rotate(String rawToken, Long userId) {
        RefreshToken currentToken = findValidToken(rawToken);

        if (!currentToken.getUserId().equals(userId)) {
            throw new AuthException(String.format(AUTHENTICATION_FAILED.getErrorMessage(), "Invalid refresh token"));
        }

        currentToken.revoke();

        return create(userId);
    }

    private RefreshToken findValidToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                    new AuthException(String.format(AUTHENTICATION_FAILED.getErrorMessage(), "Invalid refresh token"))
                );

        if (refreshToken.isRevoked()
                || !refreshToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new AuthException(String.format(AUTHENTICATION_FAILED.getErrorMessage(), "Invalid refresh token"));
        }

        return refreshToken;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthException(String.format(AUTHENTICATION_FAILED.getErrorMessage(), "SHA-256 is not available"));
        }
    }
}