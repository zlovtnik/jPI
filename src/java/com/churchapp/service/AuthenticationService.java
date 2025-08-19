package com.churchapp.service;

import com.churchapp.dto.auth.AuthenticationResponse;
import com.churchapp.dto.auth.LoginRequest;
import com.churchapp.dto.auth.RegisterRequest;
import com.churchapp.entity.User;
import com.churchapp.repository.MemberRepository;
import com.churchapp.repository.UserRepository;
import com.churchapp.security.ChurchUserPrincipal;
import com.churchapp.security.JwtService;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Try<AuthenticationResponse> register(RegisterRequest request) {
        return Try.of(() -> {
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            // Build user entity
            User.UserBuilder userBuilder = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .active(true);

            // Link to member if provided
            if (request.getMemberId() != null) {
                return memberRepository.findById(request.getMemberId())
                    .map(member -> {
                        User user = userBuilder.member(member).build();
                        User savedUser = userRepository.save(user);
                        return generateAuthResponse(savedUser);
                    })
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            } else {
                User user = userBuilder.build();
                User savedUser = userRepository.save(user);
                return generateAuthResponse(savedUser);
            }
        })
        .onFailure(throwable -> log.error("Failed to register user", throwable));
    }

    public Try<AuthenticationResponse> authenticate(LoginRequest request) {
        return Try.of(() -> {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            return generateAuthResponse(user);
        })
        .onFailure(throwable -> log.error("Failed to authenticate user", throwable));
    }

    public Try<AuthenticationResponse> refreshToken(String refreshToken) {
        return Try.of(() -> {
            String username = jwtService.extractUsername(refreshToken);
            
            if (username == null) {
                throw new RuntimeException("Invalid refresh token");
            }

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            ChurchUserPrincipal userPrincipal = new ChurchUserPrincipal(user);
            
            if (jwtService.isTokenValid(refreshToken, userPrincipal)) {
                return generateAuthResponse(user);
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        })
        .onFailure(throwable -> log.error("Failed to refresh token", throwable));
    }

    public Option<User> getCurrentUser(Authentication authentication) {
        return Try.of(() -> {
            if (authentication != null && authentication.getPrincipal() instanceof ChurchUserPrincipal) {
                ChurchUserPrincipal userPrincipal = (ChurchUserPrincipal) authentication.getPrincipal();
                return Option.of(userPrincipal.getUser());
            }
            return Option.<User>none();
        })
        .onFailure(throwable -> log.error("Failed to get current user", throwable))
        .getOrElse(Option.none());
    }

    private AuthenticationResponse generateAuthResponse(User user) {
        ChurchUserPrincipal userPrincipal = new ChurchUserPrincipal(user);
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        return AuthenticationResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400000L) // 24 hours in milliseconds
            .username(user.getUsername())
            .role(user.getRole().name())
            .build();
    }
}
