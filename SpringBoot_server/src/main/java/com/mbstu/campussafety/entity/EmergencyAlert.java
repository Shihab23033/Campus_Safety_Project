package com.mbstu.campussafety.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "emergency_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double accuracy;

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(
        name = "alert_responders",
        joinColumns = @JoinColumn(name = "alert_id"),
        inverseJoinColumns = @JoinColumn(name = "responder_id")
    )
    @Builder.Default
    private Set<User> assignedResponders = new HashSet<>();

    @OneToMany(mappedBy = "emergencyAlert", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Location> locationHistory = new HashSet<>();

    @OneToMany(mappedBy = "emergencyAlert", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AudioFile> audioFiles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
