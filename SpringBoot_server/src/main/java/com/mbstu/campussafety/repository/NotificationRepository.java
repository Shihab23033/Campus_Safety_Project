package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
    
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
