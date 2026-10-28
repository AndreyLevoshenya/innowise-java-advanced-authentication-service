package com.inno.service;

import com.inno.repository.model.Credential;
import com.inno.repository.model.Role;
import com.inno.service.client.UserServiceClient;
import com.inno.service.dto.*;
import com.inno.service.exception.AuthException;
import com.inno.service.exception.UserServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.inno.service.exception.ExceptionErrorCodes.AUTHENTICATION_FAILED;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final UserServiceClient userServiceClient;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, CredentialService credentialService, UserServiceClient userServiceClient) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.credentialService = credentialService;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public TokenDtoResponse register(RegisterDtoRequest request) throws UserServiceException {

        if (credentialService.existsByUsername(request.username())) {
            throw new AuthException(String.format(AUTHENTICATION_FAILED.getErrorMessage(), "Username already exists"));
        }

        String serviceAccessToken = jwtService.generateServiceAccessToken();

        UserDtoResponse user = userServiceClient.createUser(
                new CreateUserRequest(
                        request.name(),
                        request.surname(),
                        request.birthDate(),
                        request.email(),
                        request.active()), serviceAccessToken);

        Credential credential = new Credential(
                user.id(),
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );

        credentialService.save(credential);

        String accessToken = jwtService.generateAccessToken(credential);
        String refreshToken = refreshTokenService.create(user.id());

        return new TokenDtoResponse(accessToken, refreshToken);
    }

    public TokenDtoResponse login(LoginDtoRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        Credential credential = (Credential) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(credential);
        String refreshToken = refreshTokenService.create(credential.getUserId());

        return new TokenDtoResponse(accessToken, refreshToken);
    }


    public TokenDtoResponse refresh(RefreshDtoRequest request) {
        Long userId = refreshTokenService.validateAndGetUserId(
                request.refreshToken()
        );

        Credential credential = credentialService.findByUserId(userId);

        String accessToken = jwtService.generateAccessToken(credential);
        String refreshToken = refreshTokenService.rotate(
                request.refreshToken(),
                userId
        );

        return new TokenDtoResponse(accessToken, refreshToken);
    }

}
