package com.rgbradford.backend.service.interfaces;

import com.rgbradford.backend.dto.request.RegisterRequest;
import com.rgbradford.backend.dto.request.LoginRequest;
import com.rgbradford.backend.dto.request.RefreshTokenRequest;
import com.rgbradford.backend.dto.response.AuthResponse;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void changePassword(String email, String currentPassword, String newPassword);
}