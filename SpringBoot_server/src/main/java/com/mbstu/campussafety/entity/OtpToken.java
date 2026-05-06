package com.mbstu.campussafety.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    // Check if OTP is still valid (not expired and not used)
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    // Check if OTP has expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(10); // 10 minutes expiration
    }
}
