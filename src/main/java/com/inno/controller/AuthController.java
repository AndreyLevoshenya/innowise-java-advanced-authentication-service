package com.inno.controller;

import com.inno.service.AuthService;
import com.inno.service.dto.LoginDtoRequest;
import com.inno.service.dto.RefreshDtoRequest;
import com.inno.service.dto.RegisterDtoRequest;
import com.inno.service.dto.TokenDtoResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenDtoResponse> register(@Valid @RequestBody RegisterDtoRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDtoResponse> login(@Valid @RequestBody LoginDtoRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDtoResponse> refresh(
            @Valid @RequestBody RefreshDtoRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
