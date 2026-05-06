package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.dto.auth.*;
import com.mbstu.campussafety.dto.user.UserDTO;
import com.mbstu.campussafety.service.AuthService;
import com.mbstu.campussafety.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints for user registration, login, and account management")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with email and password")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration request for email: {}", request.getEmail());
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("User registered successfully. Please verify your email.")
        );
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login request for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify email with OTP code")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        log.info("OTP verification request for email: {}", request.getEmail());
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Resend OTP to user email")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody PasswordResetRequest request) {
        log.info("OTP resend request for email: {}", request.getEmail());
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your email"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset link to email")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());
        authService.sendPasswordResetEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset link sent to your email"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        log.info("Password reset attempt with token: {}", request.getToken());
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get profile of currently authenticated user")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("User not authenticated")
            );
        }

        Long userId = (Long) authentication.getPrincipal();
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User profile retrieved successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current user (token invalidation handled by client)")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("User logout request");
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
