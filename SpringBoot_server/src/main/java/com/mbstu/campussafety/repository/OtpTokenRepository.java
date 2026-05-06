package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    // Find the latest valid OTP for an email (not expired, not used)
    @Query("SELECT o FROM OtpToken o WHERE o.email = :email AND o.used = false AND o.expiresAt > CURRENT_TIMESTAMP ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpToken> findLatestValidOtpByEmail(@Param("email") String email);
    
    // Find all OTPs for an email (for cleanup)
    List<OtpToken> findByEmail(String email);
    
    // Delete expired OTPs (cleanup task)
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredOtps();
}
