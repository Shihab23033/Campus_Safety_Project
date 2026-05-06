package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByStatus(String status);
    List<EmergencyAlert> findByUserId(Long userId);
    List<EmergencyAlert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM EmergencyAlert a WHERE a.status = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<EmergencyAlert> findActiveAlerts();
    
    @Query("SELECT a FROM EmergencyAlert a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<EmergencyAlert> findUserAlerts(@Param("userId") Long userId);
}
