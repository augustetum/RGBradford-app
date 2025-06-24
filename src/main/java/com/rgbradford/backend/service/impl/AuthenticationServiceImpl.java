package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.service.interfaces.AuthenticationService;
import com.rgbradford.backend.dto.request.RegisterRequest;
import com.rgbradford.backend.dto.request.LoginRequest;
import com.rgbradford.backend.dto.response.AuthResponse;
import com.rgbradford.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.rgbradford.backend.entity.User;

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
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }
        
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .build();
        userRepository.save(user);
        // TODO: Replace with real JWT token
        return new AuthResponse("dummy-token");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password for user: " + request.getEmail());
        }
        
        // TODO: Replace with real JWT token
        return new AuthResponse("dummy-token");
    }
} 