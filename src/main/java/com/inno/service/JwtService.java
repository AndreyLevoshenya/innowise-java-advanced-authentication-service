package com.inno.service;

import com.inno.repository.model.Credential;
import com.inno.repository.model.Role;
import com.inno.service.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.inno.service.exception.ExceptionErrorCodes.AUTHENTICATION_FAILED;

@Service
public class JwtService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JwtService.class);
    private static final int SERVICE_TTL = 300;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final Duration ttl;

    public JwtService(
            @Value("${jwt.private-key}") Resource privateKeyResource,
            @Value("${jwt.public-key}") Resource publicKeyResource,
            @Value("${jwt.ttl}") Duration ttl
    ) throws IOException, GeneralSecurityException {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey = loadPublicKey(publicKeyResource);
        this.ttl = ttl;
    }

    public String generateAccessToken(Credential credential) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", credential.getUserId());
        claims.put(
                "authorities",
                credential.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );

        LOGGER.debug(
                "Generating access token for user {}",
                credential.getUsername()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(credential.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateServiceAccessToken() {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put(
                "authorities",
                List.of(Role.SERVICE)
        );

        LOGGER.debug("Generating access token for auth service");

        return Jwts.builder()
                .claims(claims)
                .subject("auth-service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(SERVICE_TTL)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object value = extractAllClaims(token).get("userId");

        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new AuthException(String.format(AUTHENTICATION_FAILED.getErrorMessage(), "Missing or invalid userId claim"));
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            LOGGER.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(
            String token,
            UserDetails userDetails
    ) {
        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject().equals(
                    userDetails.getUsername()
            );
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey loadPrivateKey(Resource resource)
            throws IOException, GeneralSecurityException {
        String pem = readPem(resource);
        byte[] encoded = Base64.getDecoder().decode(
                pem.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "")
        );

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePrivate(
                new PKCS8EncodedKeySpec(encoded)
        );
    }

    private PublicKey loadPublicKey(Resource resource)
            throws IOException, GeneralSecurityException {
        String pem = readPem(resource);
        byte[] encoded = Base64.getDecoder().decode(
                pem.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "")
        );

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePublic(
                new X509EncodedKeySpec(encoded)
        );
    }

    private String readPem(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}
