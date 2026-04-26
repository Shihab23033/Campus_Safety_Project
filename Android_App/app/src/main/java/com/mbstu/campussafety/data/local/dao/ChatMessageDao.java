package com.mbstu.campussafety.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mbstu.campussafety.data.local.entity.ChatMessageEntity;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatMessageEntity message);

    @Update
    void update(ChatMessageEntity message);

    @Delete
    void delete(ChatMessageEntity message);

    @Query("SELECT * FROM chat_messages WHERE messageId = :messageId")
    ChatMessageEntity getMessageById(String messageId);

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp DESC")
    List<ChatMessageEntity> getConversation(String userId1, String userId2);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    List<ChatMessageEntity> getAllMessages();

    @Query("DELETE FROM chat_messages")
    void deleteAllMessages();
}
