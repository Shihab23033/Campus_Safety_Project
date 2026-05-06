package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.entity.SafeZone;
import com.mbstu.campussafety.service.SafeZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/safe-zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Safe Zones", description = "Safe zone management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class SafeZoneController {

    private final SafeZoneService safeZoneService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create safe zone", description = "Create new safe zone (ADMIN only)")
    public ResponseEntity<ApiResponse<SafeZone>> createSafeZone(@RequestBody CreateSafeZoneRequest request) {
        log.info("Creating safe zone: {}", request.getName());

        SafeZone safeZone = safeZoneService.createSafeZone(
            request.getName(),
            request.getLatitude(),
            request.getLongitude(),
            request.getRadius(),
            request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(safeZone, "Safe zone created successfully")
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update safe zone", description = "Update existing safe zone (ADMIN only)")
    public ResponseEntity<ApiResponse<SafeZone>> updateSafeZone(
            @PathVariable Long id,
            @RequestBody CreateSafeZoneRequest request) {
        log.info("Updating safe zone: {}", id);

        SafeZone safeZone = safeZoneService.updateSafeZone(
            id,
            request.getName(),
            request.getLatitude(),
            request.getLongitude(),
            request.getRadius(),
            request.getDescription()
        );

        return ResponseEntity.ok(ApiResponse.success(safeZone, "Safe zone updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete safe zone", description = "Delete existing safe zone (ADMIN only)")
    public ResponseEntity<ApiResponse<String>> deleteSafeZone(@PathVariable Long id) {
        log.info("Deleting safe zone: {}", id);

        safeZoneService.deleteSafeZone(id);
        return ResponseEntity.ok(ApiResponse.success("Safe zone deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all safe zones", description = "Retrieve all safe zones")
    public ResponseEntity<ApiResponse<List<SafeZone>>> getAllSafeZones() {
        log.debug("Fetching all safe zones");

        List<SafeZone> zones = safeZoneService.getAllSafeZones();
        return ResponseEntity.ok(ApiResponse.success(zones, "Safe zones retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get safe zone by ID", description = "Retrieve specific safe zone details")
    public ResponseEntity<ApiResponse<SafeZone>> getSafeZone(@PathVariable Long id) {
        log.debug("Fetching safe zone: {}", id);

        SafeZone zone = safeZoneService.getSafeZoneById(id);
        return ResponseEntity.ok(ApiResponse.success(zone, "Safe zone retrieved successfully"));
    }

    // Inner class for request body
    public static class CreateSafeZoneRequest {
        private String name;
        private Double latitude;
        private Double longitude;
        private Double radius;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public Double getRadius() { return radius; }
        public void setRadius(Double radius) { this.radius = radius; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
