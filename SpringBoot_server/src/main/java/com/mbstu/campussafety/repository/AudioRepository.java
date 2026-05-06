package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.AudioFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AudioRepository extends JpaRepository<AudioFile, Long> {
    Page<AudioFile> findByEmergencyAlertId(Long alertId, Pageable pageable);
    List<AudioFile> findByEmergencyAlertId(Long alertId);
    Page<AudioFile> findByUploadedById(Long userId, Pageable pageable);
    List<AudioFile> findByUploadedAtBetween(LocalDateTime start, LocalDateTime end);
}
