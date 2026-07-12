package com.abhinav.lms.auth.service;

import com.abhinav.lms.auth.dto.JwtAuthenticationResponse;
import com.abhinav.lms.auth.dto.LoginRequest;
import com.abhinav.lms.auth.dto.SignupRequest;
import com.abhinav.lms.auth.dto.TokenRefreshRequest;
import com.abhinav.lms.auth.entity.RefreshToken;
import com.abhinav.lms.auth.repository.RefreshTokenRepository;
import com.abhinav.lms.exception.DuplicateResourceException;
import com.abhinav.lms.exception.TokenRefreshException;
import com.abhinav.lms.security.config.JwtProperties;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.security.service.JwtService;
import com.abhinav.lms.user.dto.UserResponse;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.abhinav.lms.user.mapper.UserMapper;
import com.abhinav.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public UserResponse signup(SignupRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT); // Self-registration defaults to STUDENT

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new TokenRefreshException("Login", "User reference not found"));

        String accessToken = jwtService.generateToken(userPrincipal);
        RefreshToken refreshToken = createOrUpdateRefreshToken(user);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse refresh(TokenRefreshRequest request) {
        String tokenStr = request.getRefreshToken();
        log.info("Refreshing token session");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new TokenRefreshException(tokenStr, "Refresh token is not database registered"));

        // Verify Expiration
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(tokenStr, "Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        
        // Generate Access Token
        String accessToken = jwtService.generateToken(userPrincipal);
        
        // Rotate Refresh Token
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs()));
        RefreshToken updatedToken = refreshTokenRepository.save(refreshToken);

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(updatedToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        log.info("Logging out and revoking session token");
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(refreshTokenRepository::delete);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private RefreshToken createOrUpdateRefreshToken(User user) {
        Instant expiryInstant = Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs());
        String tokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setToken(tokenStr);
                    existingToken.setExpiryDate(expiryInstant);
                    return existingToken;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .token(tokenStr)
                        .expiryDate(expiryInstant)
                        .build()
                );

        return refreshTokenRepository.save(refreshToken);
    }
}
