package com.mbstu.campussafety.repository;

import com.mbstu.campussafety.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    Optional<GroupChat> findByEmergencyAlertId(Long alertId);
}
