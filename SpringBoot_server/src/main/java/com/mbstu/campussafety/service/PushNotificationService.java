package com.mbstu.campussafety.service;

import com.mbstu.campussafety.entity.Notification;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.NotificationRepository;
import com.mbstu.campussafety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PushNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Notification createNotification(Long userId, String title, String message, String type) {
        log.debug("Creating notification for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .message(message)
            .type(type)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();

        return notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId) {
        log.debug("Marking notification {} as read", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long countUnreadNotifications(Long userId) {
        log.debug("Counting unread notifications for user: {}", userId);
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void deleteNotification(Long notificationId) {
        log.debug("Deleting notification: {}", notificationId);

        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notificationRepository.deleteById(notificationId);
    }

    public List<Notification> getNotificationsByType(String type) {
        log.debug("Fetching notifications of type: {}", type);
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    // Notification types constants
    public static final String ALERT_CREATED = "ALERT_CREATED";
    public static final String RESPONDER_ASSIGNED = "RESPONDER_ASSIGNED";
    public static final String ALERT_RESOLVED = "ALERT_RESOLVED";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
}
