package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.entity.Notification;
import com.mbstu.campussafety.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Push notification management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final PushNotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Get paginated list of user notifications")
    public ResponseEntity<ApiResponse<Page<Notification>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Fetching notifications for current user");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count", description = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        log.debug("Fetching unread notification count");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread notification count retrieved"));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark notification as read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        log.debug("Marking notification {} as read", id);

        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a notification")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id) {
        log.info("Deleting notification: {}", id);

        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send test notification", description = "Send test notification (ADMIN only)")
    public ResponseEntity<ApiResponse<Notification>> sendTestNotification(
            @RequestBody SendNotificationRequest request) {
        log.info("Sending test notification to user: {}", request.getUserId());

        Notification notification = notificationService.createNotification(
            request.getUserId(),
            request.getTitle(),
            request.getMessage(),
            request.getType()
        );

        return ResponseEntity.ok(ApiResponse.success(notification, "Notification sent successfully"));
    }

    // Inner class for request body
    public static class SendNotificationRequest {
        private Long userId;
        private String title;
        private String message;
        private String type;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
