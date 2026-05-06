package com.mbstu.campussafety.controller;

import com.mbstu.campussafety.entity.ChatMessage;
import com.mbstu.campussafety.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat/send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessagePayload payload, SimpMessageHeaderAccessor headerAccessor) {
        log.debug("WebSocket message received from user: {}", payload.getSenderId());

        ChatMessage message = chatService.sendMessage(
            payload.getSenderId(),
            payload.getRecipientId(),
            payload.getMessage(),
            false
        );

        return message;
    }

    @MessageMapping("/chat/group/send")
    @SendTo("/topic/group-messages")
    public ChatMessage sendGroupMessage(@Payload GroupChatMessagePayload payload, SimpMessageHeaderAccessor headerAccessor) {
        log.debug("WebSocket group message received from user: {}", payload.getSenderId());

        ChatMessage message = chatService.sendGroupMessage(
            payload.getSenderId(),
            payload.getGroupChatId(),
            payload.getMessage()
        );

        return message;
    }

    @MessageMapping("/chat/typing")
    @SendTo("/topic/typing")
    public TypingIndicator sendTypingIndicator(@Payload TypingIndicator indicator) {
        log.debug("User {} is typing", indicator.getUserId());
        return indicator;
    }

    // Inner classes for payload
    public static class ChatMessagePayload {
        public Long senderId;
        public Long recipientId;
        public String message;

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class GroupChatMessagePayload {
        public Long senderId;
        public Long groupChatId;
        public String message;

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public Long getGroupChatId() { return groupChatId; }
        public void setGroupChatId(Long groupChatId) { this.groupChatId = groupChatId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class TypingIndicator {
        public Long userId;
        public String userName;
        public Long recipientId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    }
}
