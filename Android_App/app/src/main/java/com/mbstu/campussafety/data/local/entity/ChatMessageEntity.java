package com.mbstu.campussafety.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String messageId;
    public String senderId;
    public String receiverId;
    public String message;
    public long timestamp;
    public boolean isRead;
}
