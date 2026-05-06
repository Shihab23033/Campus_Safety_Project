package com.mbstu.campussafety.service;

import com.mbstu.campussafety.entity.SafeZone;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.SafeZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SafeZoneService {

    private final SafeZoneRepository safeZoneRepository;

    public SafeZone createSafeZone(String name, Double latitude, Double longitude, Double radius, String description) {
        log.info("Creating safe zone: {}", name);

        SafeZone safeZone = SafeZone.builder()
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .radius(radius)
            .description(description)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return safeZoneRepository.save(safeZone);
    }

    public SafeZone updateSafeZone(Long id, String name, Double latitude, Double longitude, Double radius, String description) {
        log.info("Updating safe zone: {}", id);

        SafeZone safeZone = safeZoneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Safe zone not found"));

        safeZone.setName(name);
        safeZone.setLatitude(latitude);
        safeZone.setLongitude(longitude);
        safeZone.setRadius(radius);
        safeZone.setDescription(description);
        safeZone.setUpdatedAt(LocalDateTime.now());

        return safeZoneRepository.save(safeZone);
    }

    public void deleteSafeZone(Long id) {
        log.info("Deleting safe zone: {}", id);

        if (!safeZoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Safe zone not found");
        }

        safeZoneRepository.deleteById(id);
    }

    public SafeZone getSafeZoneById(Long id) {
        log.debug("Fetching safe zone: {}", id);
        return safeZoneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Safe zone not found"));
    }

    public List<SafeZone> getAllSafeZones() {
        log.debug("Fetching all safe zones");
        return safeZoneRepository.findAll();
    }
}
