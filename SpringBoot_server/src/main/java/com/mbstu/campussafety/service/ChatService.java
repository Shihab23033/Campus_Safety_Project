package com.mbstu.campussafety.service;

import com.mbstu.campussafety.entity.ChatMessage;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.entity.GroupChat;
import com.mbstu.campussafety.entity.EmergencyAlert;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.ChatRepository;
import com.mbstu.campussafety.repository.GroupChatRepository;
import com.mbstu.campussafety.repository.UserRepository;
import com.mbstu.campussafety.repository.EmergencyAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;

    public ChatMessage sendMessage(Long senderId, Long recipientId, String message, Boolean isGroupChat) {
        log.debug("Sending message from user {} to user {}", senderId, recipientId);

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        ChatMessage chatMessage = ChatMessage.builder()
            .sender(sender)
            .recipient(recipient)
            .message(message)
            .isGroupChat(false)
            .isRead(false)
            .timestamp(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

        return chatRepository.save(chatMessage);
    }

    public ChatMessage sendGroupMessage(Long senderId, Long groupChatId, String message) {
        log.debug("Sending group message from user {} to group {}", senderId, groupChatId);

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        GroupChat groupChat = groupChatRepository.findById(groupChatId)
            .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));

        ChatMessage chatMessage = ChatMessage.builder()
            .sender(sender)
            .groupChat(groupChat)
            .message(message)
            .isGroupChat(true)
            .isRead(false)
            .timestamp(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

        return chatRepository.save(chatMessage);
    }

    public GroupChat createGroupChat(Long alertId) {
        log.info("Creating group chat for alert: {}", alertId);

        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        // Check if group chat already exists
        Optional<GroupChat> existingChat = groupChatRepository.findByEmergencyAlertId(alertId);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }

        Set<User> participants = new HashSet<>(alert.getAssignedResponders());
        participants.add(alert.getUser());

        GroupChat groupChat = GroupChat.builder()
            .emergencyAlert(alert)
            .participants(participants)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return groupChatRepository.save(groupChat);
    }

    public long countUnreadMessages(Long userId) {
        log.debug("Counting unread messages for user: {}", userId);
        return chatRepository.countUnreadMessages(userId);
    }

    public void markMessageAsRead(Long messageId) {
        log.debug("Marking message {} as read", messageId);
        ChatMessage message = chatRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        message.setIsRead(true);
        chatRepository.save(message);
    }
}
