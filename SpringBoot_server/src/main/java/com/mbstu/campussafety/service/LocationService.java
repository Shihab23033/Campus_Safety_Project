package com.mbstu.campussafety.service;

import com.mbstu.campussafety.entity.Location;
import com.mbstu.campussafety.entity.EmergencyAlert;
import com.mbstu.campussafety.entity.SafeZone;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.LocationRepository;
import com.mbstu.campussafety.repository.SafeZoneRepository;
import com.mbstu.campussafety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final SafeZoneRepository safeZoneRepository;
    private final UserRepository userRepository;

    @Async
    public void updateLocation(Long userId, Double latitude, Double longitude, Double accuracy) {
        log.debug("Updating location for user: {} at ({}, {})", userId, latitude, longitude);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Location location = Location.builder()
            .user(user)
            .latitude(latitude)
            .longitude(longitude)
            .accuracy(accuracy != null ? accuracy : 0.0)
            .timestamp(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

        locationRepository.save(location);
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        userRepository.save(user);
    }

    public Optional<Location> getLatestLocation(Long userId) {
        log.debug("Fetching latest location for user: {}", userId);
        return locationRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Location> getLocationHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching location history for user {} between {} and {}", userId, startDate, endDate);
        return locationRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
    }

    public List<Location> getAlertLocationHistory(Long alertId) {
        log.debug("Fetching location history for alert: {}", alertId);
        return locationRepository.findAlertLocationHistory(alertId);
    }

    public boolean isUserInSafeZone(Long userId) {
        log.debug("Checking if user {} is in safe zone", userId);

        Optional<Location> locationOpt = getLatestLocation(userId);
        if (locationOpt.isEmpty()) {
            return false;
        }

        Location location = locationOpt.get();
        List<SafeZone> safeZones = safeZoneRepository.findAll();

        return safeZones.stream()
            .anyMatch(zone -> isWithinSafeZone(location.getLatitude(), location.getLongitude(), zone));
    }

    public boolean isWithinSafeZone(Double latitude, Double longitude, SafeZone safeZone) {
        double distance = calculateDistance(latitude, longitude, safeZone.getLatitude(), safeZone.getLongitude());
        return distance <= safeZone.getRadius();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
