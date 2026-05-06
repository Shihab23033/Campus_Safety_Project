package com.mbstu.campussafety.service;

import com.mbstu.campussafety.dto.auth.*;
import com.mbstu.campussafety.dto.user.UserDTO;
import com.mbstu.campussafety.entity.Role;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.exception.BadRequestException;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.exception.UnauthorizedException;
import com.mbstu.campussafety.repository.RoleRepository;
import com.mbstu.campussafety.repository.UserRepository;
import com.mbstu.campussafety.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final OtpService otpService;

    // Password reset tokens (consider moving to database with expiration)
    private final Map<String, String> passwordResetTokens = new HashMap<>();

    /**
     * Register new user with email verification via OTP
     * Flow: Generate OTP → Send email → Save user (if email succeeds)
     */
    public void register(UserRegistrationRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        try {
            // Step 1: Generate OTP and save to database
            String otp = otpService.generateAndSaveOtp(request.getEmail());
            log.debug("OTP generated for email: {}", request.getEmail());

            // Step 2: Send OTP email (if this fails, user is not saved)
            try {
                emailService.sendOtpEmail(request.getEmail(), otp);
                log.info("OTP email sent successfully to: {}", request.getEmail());
            } catch (Exception e) {
                log.error("Failed to send OTP email to: {}. User registration aborted. Error: {}", 
                    request.getEmail(), e.getMessage(), e);
                otpService.invalidateOtpsForEmail(request.getEmail());
                throw new RuntimeException("Failed to send verification email. Please try registering again later.", e);
            }

            // Step 3: Only save user to database AFTER email is successfully sent
            User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isVerified(false)
                .build();

            // Assign MEMBER role by default
            Role memberRole = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new ResourceNotFoundException("MEMBER role not found"));
            user.setRoles(new HashSet<>(Collections.singletonList(memberRole)));

            userRepository.save(user);
            log.info("User registered successfully: {}", request.getEmail());

        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getIsVerified()) {
            throw new UnauthorizedException("Email not verified. Please verify your email first.");
        }

        String token = jwtTokenProvider.generateToken(user);
        
        return LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .user(mapUserToDTO(user))
            .build();
    }

    /**
     * Verify OTP and mark user email as verified
     */
    public void verifyOtp(OtpVerificationRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        try {
            // Verify OTP using OtpService (throws exception if invalid/expired)
            otpService.verifyOtp(request.getEmail(), request.getOtp());

            // Find user and mark as verified
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            user.setIsVerified(true);
            userRepository.save(user);

            log.info("Email verified successfully for user: {}", request.getEmail());
        } catch (BadRequestException e) {
            log.warn("OTP verification failed for email: {}", request.getEmail());
            throw e;
        } catch (Exception e) {
            log.error("Error during OTP verification for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    public void sendPasswordResetEmail(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User with email not found"));

        String resetToken = UUID.randomUUID().toString();
        passwordResetTokens.put(resetToken, request.getEmail());

        try {
            emailService.sendPasswordResetEmail(request.getEmail(), resetToken);
            log.info("Password reset email sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send reset email: " + e.getMessage(), e);
        }
    }

    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token: {}", token);

        String email = passwordResetTokens.get(token);
        if (email == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokens.remove(token);
        log.info("Password reset successful for user: {}", email);
    }

    /**
     * Resend OTP to user email
     */
    public void resendOtp(String email) {
        log.info("Resending OTP for email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User with email not found"));

        if (user.getIsVerified()) {
            throw new BadRequestException("User already verified");
        }

        try {
            // Generate new OTP
            String otp = otpService.generateAndSaveOtp(email);
            log.debug("New OTP generated for email: {}", email);

            // Send OTP email
            emailService.sendOtpEmail(email, otp);
            log.info("OTP resent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to resend OTP to: {}. Error: {}", email, e.getMessage(), e);
            otpService.invalidateOtpsForEmail(email);
            throw new RuntimeException("Failed to send OTP: " + e.getMessage() + ". Please try again later or contact support.", e);
        }
    }

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .isVerified(user.getIsVerified())
            .latitude(user.getLatitude())
            .longitude(user.getLongitude())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
