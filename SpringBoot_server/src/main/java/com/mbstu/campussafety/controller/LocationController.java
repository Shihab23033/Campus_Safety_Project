package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.entity.SafeZone;
import com.mbstu.campussafety.service.LocationService;
import com.mbstu.campussafety.service.SafeZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Location Tracking", description = "Location tracking and GPS endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class LocationController {

    private final LocationService locationService;
    private final SafeZoneService safeZoneService;

    @PostMapping("/update")
    @Operation(summary = "Update user location", description = "Send GPS coordinates from mobile app")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double accuracy) {
        log.debug("Location update request for lat: {}, lon: {}", latitude, longitude);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        locationService.updateLocation(userId, latitude, longitude, accuracy);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully"));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current location", description = "Get current GPS location of authenticated user")
    public ResponseEntity<ApiResponse<?>> getCurrentLocation() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        var location = locationService.getLatestLocation(userId);
        return ResponseEntity.ok(ApiResponse.success(location, "Current location retrieved"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get location history", description = "Get GPS location history for date range")
    public ResponseEntity<ApiResponse<?>> getLocationHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        var history = locationService.getLocationHistory(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(history, "Location history retrieved"));
    }

    @GetMapping("/safe-zones")
    @Operation(summary = "Get safe zones", description = "Get all configured safe zones")
    public ResponseEntity<ApiResponse<List<SafeZone>>> getSafeZones() {
        log.debug("Fetching all safe zones");

        List<SafeZone> zones = safeZoneService.getAllSafeZones();
        return ResponseEntity.ok(ApiResponse.success(zones, "Safe zones retrieved"));
    }

    @GetMapping("/in-safe-zone")
    @Operation(summary = "Check if in safe zone", description = "Check if current user location is in any safe zone")
    public ResponseEntity<ApiResponse<?>> isInSafeZone() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        boolean inSafeZone = locationService.isUserInSafeZone(userId);
        return ResponseEntity.ok(ApiResponse.success(inSafeZone, "Safe zone check completed"));
    }
}
