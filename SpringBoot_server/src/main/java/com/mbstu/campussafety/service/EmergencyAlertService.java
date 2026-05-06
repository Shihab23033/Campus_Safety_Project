package com.mbstu.campussafety.service;

import com.mbstu.campussafety.dto.alert.CreateEmergencyAlertRequest;
import com.mbstu.campussafety.dto.alert.EmergencyAlertDTO;
import com.mbstu.campussafety.entity.EmergencyAlert;
import com.mbstu.campussafety.entity.Role;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.EmergencyAlertRepository;
import com.mbstu.campussafety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmergencyAlertService {

    private final EmergencyAlertRepository alertRepository;
    private final UserRepository userRepository;

    private static final double RESPONDER_SEARCH_RADIUS = 5.0; // 5 km radius

    public EmergencyAlertDTO createAlert(CreateEmergencyAlertRequest request, Long userId) {
        log.info("Creating emergency alert for user: {} with category: {}", userId, request.getCategory());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmergencyAlert alert = EmergencyAlert.builder()
            .user(user)
            .title(request.getTitle())
            .description(request.getDescription())
            .category(request.getCategory())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .accuracy(request.getAccuracy() != null ? request.getAccuracy() : 0.0)
            .status("ACTIVE")
            .assignedResponders(new HashSet<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // Auto-assign responders
        assignResponders(savedAlert);

        log.info("Emergency alert created successfully with ID: {}", savedAlert.getId());
        return mapAlertToDTO(savedAlert);
    }

    public EmergencyAlertDTO updateAlertStatus(Long alertId, String status) {
        log.info("Updating alert {} status to: {}", alertId, status);

        EmergencyAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        alert.setStatus(status);
        alert.setUpdatedAt(LocalDateTime.now());

        if ("RESOLVED".equals(status)) {
            alert.setResolvedAt(LocalDateTime.now());
        }

        EmergencyAlert updatedAlert = alertRepository.save(alert);
        return mapAlertToDTO(updatedAlert);
    }

    public EmergencyAlertDTO assignResponder(Long alertId, Long responderId) {
        log.info("Assigning responder {} to alert {}", responderId, alertId);

        EmergencyAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        User responder = userRepository.findById(responderId)
            .orElseThrow(() -> new ResourceNotFoundException("Responder not found"));

        alert.getAssignedResponders().add(responder);
        EmergencyAlert updatedAlert = alertRepository.save(alert);

        return mapAlertToDTO(updatedAlert);
    }

    public EmergencyAlertDTO getAlertById(Long alertId) {
        log.debug("Fetching alert with id: {}", alertId);

        EmergencyAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        return mapAlertToDTO(alert);
    }

    public List<EmergencyAlertDTO> getActiveAlerts() {
        log.debug("Fetching all active alerts");

        return alertRepository.findActiveAlerts().stream()
            .map(this::mapAlertToDTO)
            .collect(Collectors.toList());
    }

    public List<EmergencyAlertDTO> getUserAlerts(Long userId) {
        log.debug("Fetching alerts for user: {}", userId);

        return alertRepository.findUserAlerts(userId).stream()
            .map(this::mapAlertToDTO)
            .collect(Collectors.toList());
    }

    public List<EmergencyAlertDTO> getAlertHistory(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching alert history between {} and {}", startDate, endDate);

        return alertRepository.findByCreatedAtBetween(startDate, endDate).stream()
            .map(this::mapAlertToDTO)
            .collect(Collectors.toList());
    }

    private void assignResponders(EmergencyAlert alert) {
        log.debug("Auto-assigning responders for alert: {}", alert.getId());

        // Find all users with RESPONDER role
        List<User> responders = userRepository.findByRolesName("RESPONDER");

        // Filter responders within search radius
        Set<User> nearbyResponders = responders.stream()
            .filter(responder -> calculateDistance(
                alert.getLatitude(), alert.getLongitude(),
                responder.getLatitude(), responder.getLongitude()
            ) <= RESPONDER_SEARCH_RADIUS)
            .limit(5) // Assign max 5 responders
            .collect(Collectors.toSet());

        alert.setAssignedResponders(nearbyResponders);
        log.debug("Assigned {} responders to alert", nearbyResponders.size());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two points
        final int R = 6371; // Radius of earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private EmergencyAlertDTO mapAlertToDTO(EmergencyAlert alert) {
        return EmergencyAlertDTO.builder()
            .id(alert.getId())
            .userId(alert.getUser().getId())
            .userName(alert.getUser().getFirstName() + " " + alert.getUser().getLastName())
            .title(alert.getTitle())
            .description(alert.getDescription())
            .category(alert.getCategory())
            .status(alert.getStatus())
            .latitude(alert.getLatitude())
            .longitude(alert.getLongitude())
            .accuracy(alert.getAccuracy())
            .responderCount(alert.getAssignedResponders().size())
            .createdAt(alert.getCreatedAt())
            .updatedAt(alert.getUpdatedAt())
            .resolvedAt(alert.getResolvedAt())
            .build();
    }
}
