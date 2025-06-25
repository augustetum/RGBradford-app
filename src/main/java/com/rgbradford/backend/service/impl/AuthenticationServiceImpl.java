package com.rgbradford.backend.service.impl;

import com.rgbradford.backend.service.interfaces.AuthenticationService;
import com.rgbradford.backend.dto.request.RegisterRequest;
import com.rgbradford.backend.dto.request.LoginRequest;
import com.rgbradford.backend.dto.request.RefreshTokenRequest;
import com.rgbradford.backend.dto.response.AuthResponse;
import com.rgbradford.backend.repository.UserRepository;
import com.rgbradford.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.rgbradford.backend.entity.User;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository, 
                                   PasswordEncoder passwordEncoder,
                                   JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
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
        
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password for user: " + request.getEmail());
        }
        
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }
} 