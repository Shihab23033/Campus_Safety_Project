package com.mbstu.campussafety.service;

import com.mbstu.campussafety.entity.OtpToken;
import com.mbstu.campussafety.exception.BadRequestException;
import com.mbstu.campussafety.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    /**
     * Generate and save OTP for email
     * @param email Email address to generate OTP for
     * @return Generated OTP string
     */
    public String generateAndSaveOtp(String email) {
        log.info("Generating OTP for email: {}", email);
        
        // Generate secure random OTP
        String otp = generateSecureOtp();
        
        // Save to database
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .attempts(0)
                .build();
        
        otpTokenRepository.save(otpToken);
        log.info("OTP saved successfully for email: {}", email);
        
        return otp;
    }

    /**
     * Verify OTP for email
     * @param email Email address
     * @param otp OTP to verify
     * @throws BadRequestException if OTP is invalid, expired, used, or max attempts exceeded
     */
    public void verifyOtp(String email, String otp) {
        log.info("Verifying OTP for email: {}", email);
        
        OtpToken otpToken = otpTokenRepository.findLatestValidOtpByEmail(email)
                .orElseThrow(() -> {
                    log.warn("OTP not found or expired for email: {}", email);
                    return new BadRequestException("OTP not found or expired. Please request a new OTP.");
                });

        // Check if OTP has expired
        if (otpToken.isExpired()) {
            log.warn("OTP expired for email: {}", email);
            throw new BadRequestException("OTP expired. Please request a new OTP.");
        }

        // Check if OTP already used
        if (otpToken.getUsed()) {
            log.warn("OTP already used for email: {}", email);
            throw new BadRequestException("OTP already used. Please request a new OTP.");
        }

        // Check max attempts
        if (otpToken.getAttempts() >= maxAttempts) {
            log.warn("Max OTP verification attempts exceeded for email: {}", email);
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        // Verify OTP
        if (!otpToken.getOtp().equals(otp)) {
            log.warn("Invalid OTP for email: {} (attempt {}/{})", email, otpToken.getAttempts() + 1, maxAttempts);
            otpToken.setAttempts(otpToken.getAttempts() + 1);
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Invalid OTP. Please try again. Remaining attempts: " + (maxAttempts - otpToken.getAttempts()));
        }

        // Mark OTP as used
        otpToken.setUsed(true);
        otpToken.setAttempts(otpToken.getAttempts() + 1);
        otpTokenRepository.save(otpToken);
        
        log.info("OTP verified successfully for email: {}", email);
    }

    /**
     * Invalidate all OTPs for an email (for security cleanup)
     * @param email Email address
     */
    public void invalidateOtpsForEmail(String email) {
        log.info("Invalidating all OTPs for email: {}", email);
        otpTokenRepository.findByEmail(email).forEach(otp -> {
            otp.setUsed(true);
            otpTokenRepository.save(otp);
        });
    }

    /**
     * Generate secure random OTP (6 digits by default)
     * @return OTP string
     */
    private String generateSecureOtp() {
        SecureRandom random = new SecureRandom();
        int max = (int) Math.pow(10, otpLength);
        int min = (int) Math.pow(10, otpLength - 1);
        int otp = min + random.nextInt(max - min);
        return String.valueOf(otp);
    }

    /**
     * Scheduled task to clean up expired OTPs (runs every hour)
     */
    @Scheduled(fixedDelay = 3600000) // Run every hour
    public void cleanupExpiredOtps() {
        log.info("Running scheduled cleanup of expired OTPs");
        try {
            otpTokenRepository.deleteExpiredOtps();
            log.info("Expired OTPs cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during OTP cleanup", e);
        }
    }
}
