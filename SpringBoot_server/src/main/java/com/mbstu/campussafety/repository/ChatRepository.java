package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId OR m.sender.id = :recipientId AND m.recipient.id = :senderId ORDER BY m.createdAt DESC")
    Page<ChatMessage> findConversation(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId, Pageable pageable);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.groupChat.id = :groupChatId ORDER BY m.createdAt DESC")
    Page<ChatMessage> findGroupChatMessages(@Param("groupChatId") Long groupChatId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.recipient.id = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);
    
    List<ChatMessage> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
