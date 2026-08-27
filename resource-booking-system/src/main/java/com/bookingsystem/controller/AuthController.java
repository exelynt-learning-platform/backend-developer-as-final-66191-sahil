package com.bookingsystem.controller;

import com.bookingsystem.dto.auth.LoginRequest;
import com.bookingsystem.dto.auth.LoginResponse;
import com.bookingsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and JWT issuance")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements // no auth required for login itself
    @Operation(summary = "Authenticate with username/password and receive a JWT bearer token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
