package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.dto.ApiResponse;
import com.mbstu.campussafety.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Real-time chat endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/group/{alertId}")
    @Operation(summary = "Create group chat", description = "Create group chat for emergency alert")
    public ResponseEntity<ApiResponse<?>> createGroupChat(@PathVariable Long alertId) {
        log.info("Creating group chat for alert: {}", alertId);

        var groupChat = chatService.createGroupChat(alertId);
        return ResponseEntity.ok(ApiResponse.success(groupChat, "Group chat created successfully"));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread message count", description = "Get count of unread messages")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount() {
        log.debug("Fetching unread message count");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();

        long count = chatService.countUnreadMessages(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread message count retrieved"));
    }

    @PutMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read", description = "Mark specific message as read")
    public ResponseEntity<ApiResponse<String>> markMessageAsRead(@PathVariable Long messageId) {
        log.debug("Marking message {} as read", messageId);

        chatService.markMessageAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read"));
    }
}
