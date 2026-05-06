package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.dto.alert.CreateEmergencyAlertRequest;
import com.mbstu.campussafety.dto.alert.EmergencyAlertDTO;
import com.mbstu.campussafety.service.EmergencyAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Emergency Alerts", description = "Emergency alert management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class EmergencyAlertController {

    private final EmergencyAlertService alertService;

    @PostMapping
    @Operation(summary = "Create emergency alert", description = "Create a new SOS emergency alert")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO>> createAlert(@Valid @RequestBody CreateEmergencyAlertRequest request) {
        log.info("Creating emergency alert for category: {}", request.getCategory());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        EmergencyAlertDTO alert = alertService.createAlert(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(alert, "Emergency alert created successfully")
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESPONDER')")
    @Operation(summary = "Get active alerts", description = "Get all active emergency alerts (ADMIN/RESPONDER only)")
    public ResponseEntity<ApiResponse<List<EmergencyAlertDTO>>> getActiveAlerts() {
        log.debug("Fetching active alerts");

        List<EmergencyAlertDTO> alerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(ApiResponse.success(alerts, "Active alerts retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID", description = "Get specific emergency alert details")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO>> getAlertById(@PathVariable Long id) {
        log.debug("Fetching alert with id: {}", id);

        EmergencyAlertDTO alert = alertService.getAlertById(id);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user alerts", description = "Get all alerts created by specific user")
    public ResponseEntity<ApiResponse<List<EmergencyAlertDTO>>> getUserAlerts(@PathVariable Long userId) {
        log.debug("Fetching alerts for user: {}", userId);

        List<EmergencyAlertDTO> alerts = alertService.getUserAlerts(userId);
        return ResponseEntity.ok(ApiResponse.success(alerts, "User alerts retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update alert status", description = "Update emergency alert status (ACTIVE, RESOLVED, CANCELLED)")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO>> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Updating alert {} status to: {}", id, status);

        EmergencyAlertDTO alert = alertService.updateAlertStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alert status updated successfully"));
    }

    @PostMapping("/{id}/assign/{responderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign responder", description = "Assign responder to emergency alert (ADMIN only)")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO>> assignResponder(
            @PathVariable Long id,
            @PathVariable Long responderId) {
        log.info("Assigning responder {} to alert {}", responderId, id);

        EmergencyAlertDTO alert = alertService.assignResponder(id, responderId);
        return ResponseEntity.ok(ApiResponse.success(alert, "Responder assigned successfully"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get alert history", description = "Get historical emergency alerts (ADMIN only)")
    public ResponseEntity<ApiResponse<List<EmergencyAlertDTO>>> getAlertHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Fetching alert history from {} to {}", startDate, endDate);

        List<EmergencyAlertDTO> alerts = alertService.getAlertHistory(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(alerts, "Alert history retrieved successfully"));
    }
}
