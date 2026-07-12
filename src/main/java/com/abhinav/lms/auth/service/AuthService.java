package com.abhinav.lms.auth.service;

import com.abhinav.lms.auth.dto.JwtAuthenticationResponse;
import com.abhinav.lms.auth.dto.LoginRequest;
import com.abhinav.lms.auth.dto.SignupRequest;
import com.abhinav.lms.auth.dto.TokenRefreshRequest;
import com.abhinav.lms.user.dto.UserResponse;

public interface AuthService {

    UserResponse signup(SignupRequest request);

    JwtAuthenticationResponse login(LoginRequest request);

    JwtAuthenticationResponse refresh(TokenRefreshRequest request);

    void logout(String refreshToken);
}
