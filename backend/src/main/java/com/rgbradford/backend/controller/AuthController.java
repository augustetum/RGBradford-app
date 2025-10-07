package com.rgbradford.backend.controller;

import com.rgbradford.backend.service.interfaces.AuthenticationService;
import com.rgbradford.backend.dto.request.ChangePasswordRequest;
import com.rgbradford.backend.dto.request.RegisterRequest;
import com.rgbradford.backend.dto.request.LoginRequest;
import com.rgbradford.backend.dto.request.RefreshTokenRequest;
import com.rgbradford.backend.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Authentication",
    description = "APIs for user authentication, registration, and token management"
)
public class AuthController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and returns authentication tokens. " +
                "The email must be unique and not already registered in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful registration",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenType": "Bearer"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or email already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Email already exists"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                        name = "Registration request",
                        value = """
                            {
                              "email": "user@example.com",
                              "password": "SecurePassword123!"
                            }
                            """
                    )
                )
            )
            @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password, returning access and refresh tokens"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful login",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenType": "Bearer"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Invalid email or password"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User login credentials",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                        name = "Login request",
                        value = """
                            {
                              "email": "user@example.com",
                              "password": "SecurePassword123!"
                            }
                            """
                    )
                )
            )
            @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }
    @Operation(
        summary = "Refresh access token",
        description = "Generates a new access token using a valid refresh token. " +
                "Use this endpoint when the access token has expired."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully refreshed token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful token refresh",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenType": "Bearer"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Invalid refresh token"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token request",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RefreshTokenRequest.class),
                    examples = @ExampleObject(
                        name = "Refresh token request",
                        value = """
                            {
                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                            }
                            """
                    )
                )
            )
            @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Change user password",
        description = "Changes the password for the currently authenticated user. " +
                "Requires a valid JWT token and the current password for verification.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Password successfully changed"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid current password or password validation failed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Current password is incorrect"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - missing or invalid token"
        )
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Password change request containing current and new passwords",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ChangePasswordRequest.class),
                    examples = @ExampleObject(
                        name = "Change password request",
                        value = """
                            {
                              "currentPassword": "OldPassword123!",
                              "newPassword": "NewSecurePassword456!"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        authenticationService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}