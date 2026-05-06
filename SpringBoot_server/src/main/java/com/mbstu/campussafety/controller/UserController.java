package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.dto.user.UserDTO;
import com.mbstu.campussafety.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get list of all users (ADMIN only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        log.debug("Fetching all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user profile by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.debug("Fetching user with id: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Get user profile by email")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        log.debug("Fetching user with email: {}", email);
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Get all users with specific role (ADMIN only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByRole(@PathVariable String roleName) {
        log.debug("Fetching users with role: {}", roleName);
        List<UserDTO> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @PostMapping("/{id}/fcm-token")
    @Operation(summary = "Update FCM token", description = "Update Firebase Cloud Messaging token for push notifications")
    public ResponseEntity<ApiResponse<String>> updateFcmToken(
            @PathVariable Long id,
            @RequestBody FcmTokenRequest request) {
        log.debug("Updating FCM token for user: {}", id);
        userService.updateFcmToken(id, request.getFcmToken());
        return ResponseEntity.ok(ApiResponse.success("FCM token updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user account (ADMIN only)")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}

// Inner class for FCM token update request
class FcmTokenRequest {
    private String fcmToken;

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
