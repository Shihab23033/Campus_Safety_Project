package com.mbstu.campussafety.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlertDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String description;
    private String category;
    private String status;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private Integer responderCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
