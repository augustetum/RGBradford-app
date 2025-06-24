package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.service.interfaces.AuthenticationService;
import com.rgbradford.backend.dto.request.RegisterRequest;
import com.rgbradford.backend.dto.request.LoginRequest;
import com.rgbradford.backend.dto.response.AuthResponse;
import com.rgbradford.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        //TODO: Implement registration logic
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        //TODO: Implement login logic
        return null;
    }
} 