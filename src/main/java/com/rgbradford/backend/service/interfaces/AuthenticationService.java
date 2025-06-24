package com.rgbradford.backend.service.interfaces;

import com.rgbradford.backend.service.dto.RegisterRequest;
import com.rgbradford.backend.service.dto.LoginRequest;
import com.rgbradford.backend.service.dto.AuthResponse;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
} 