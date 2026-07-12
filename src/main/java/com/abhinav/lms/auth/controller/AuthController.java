package com.abhinav.lms.auth.controller;

import com.abhinav.lms.auth.dto.JwtAuthenticationResponse;
import com.abhinav.lms.auth.dto.LoginRequest;
import com.abhinav.lms.auth.dto.SignupRequest;
import com.abhinav.lms.auth.dto.TokenRefreshRequest;
import com.abhinav.lms.auth.service.AuthService;
import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1 + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Endpoints for user registration, authentication, token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user account")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ApiResponse.created(response, "User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate credentials and obtain JWT tokens")
    public ApiResponse<JwtAuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ApiResponse.success(response, "User authenticated successfully");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token to obtain a new access token")
    public ApiResponse<JwtAuthenticationResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        JwtAuthenticationResponse response = authService.refresh(request);
        return ApiResponse.success(response, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out user and revoke refresh token sessions")
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
    }
}
