package com.churchapp.controller;

import com.churchapp.dto.auth.AuthenticationResponse;
import com.churchapp.dto.auth.LoginRequest;
import com.churchapp.dto.auth.RegisterRequest;
import com.churchapp.service.AuthenticationService;
import io.vavr.control.Try;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can register new users
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authenticationService.register(request)
            .fold(
                throwable -> {
                    log.error("Registration failed", throwable);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Registration failed: " + throwable.getMessage());
                },
                response -> ResponseEntity.status(HttpStatus.CREATED).body(response)
            );
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequest request) {
        return authenticationService.authenticate(request)
            .fold(
                throwable -> {
                    log.error("Authentication failed", throwable);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication failed: " + throwable.getMessage());
                },
                ResponseEntity::ok
            );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        return authenticationService.refreshToken(refreshToken)
            .fold(
                throwable -> {
                    log.error("Token refresh failed", throwable);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token refresh failed: " + throwable.getMessage());
                },
                ResponseEntity::ok
            );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return authenticationService.getCurrentUser(authentication)
            .fold(
                () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated"),
                user -> ResponseEntity.ok(user)
            );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // In a stateless JWT setup, logout is handled client-side by removing the token
        // For enhanced security, you could implement a token blacklist
        return ResponseEntity.ok("Logged out successfully");
    }
}
