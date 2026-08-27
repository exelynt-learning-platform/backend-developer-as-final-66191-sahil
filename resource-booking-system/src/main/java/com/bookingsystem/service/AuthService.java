package com.bookingsystem.service;

import com.bookingsystem.dto.auth.LoginRequest;
import com.bookingsystem.dto.auth.LoginResponse;
import com.bookingsystem.entity.User;
import com.bookingsystem.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // Throws BadCredentialsException (handled globally -> 401) on invalid username/password
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user, user.getRole().name());

        return new LoginResponse(token, "Bearer", user.getUsername(), user.getRole().name(), jwtService.getExpirationMs());
    }
}
