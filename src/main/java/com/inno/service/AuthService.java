package com.inno.service;

import com.inno.service.client.UserServiceClient;
import com.inno.service.exception.UserServiceException;
import com.inno.repository.CredentialRepository;
import com.inno.repository.model.Credential;
import com.inno.repository.model.Role;
import com.inno.service.dto.CreateUserRequest;
import com.inno.service.dto.LoginDtoRequest;
import com.inno.service.dto.RefreshDtoRequest;
import com.inno.service.dto.RegisterDtoRequest;
import com.inno.service.dto.TokenDtoResponse;
import com.inno.service.dto.UserDtoResponse;
import com.inno.service.exception.AuthException;
import com.inno.service.exception.NotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.inno.service.exception.ExceptionErrorCodes.AUTHENTICATION_FAILED;
import static com.inno.service.exception.ExceptionErrorCodes.CREDENTIAL_NOT_FOUND;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final CredentialRepository credentialRepository;
    private final UserServiceClient userServiceClient;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, CredentialRepository credentialRepository, UserServiceClient userServiceClient) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.credentialRepository = credentialRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public TokenDtoResponse register(RegisterDtoRequest request) throws UserServiceException {

        if (credentialRepository.existsByUsername(request.username())) {
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

        credentialRepository.save(credential);

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

        Credential credential = credentialRepository.findByUserId(userId).orElseThrow(
                () -> new NotFoundException(String.format(CREDENTIAL_NOT_FOUND.getErrorMessage(), userId)));

        String accessToken = jwtService.generateAccessToken(credential);
        String refreshToken = refreshTokenService.rotate(
                request.refreshToken(),
                userId
        );

        return new TokenDtoResponse(accessToken, refreshToken);
    }

}
