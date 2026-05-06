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
@Table(name = "group_chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emergency_alert_id", nullable = false)
    private EmergencyAlert emergencyAlert;

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(
        name = "group_chat_participants",
        joinColumns = @JoinColumn(name = "group_chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "groupChat", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ChatMessage> messages = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
