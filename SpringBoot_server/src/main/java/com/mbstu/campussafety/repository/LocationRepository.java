package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Location> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    List<Location> findByEmergencyAlertIdOrderByCreatedAtDesc(Long alertId);
    
    Page<Location> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT l FROM Location l WHERE l.emergencyAlert.id = :alertId ORDER BY l.createdAt DESC")
    List<Location> findAlertLocationHistory(@Param("alertId") Long alertId);
}
