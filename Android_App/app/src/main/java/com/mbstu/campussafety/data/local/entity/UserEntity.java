package com.mbstu.campussafety.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;
    public String email;
    public String first_name;

    public String last_name;
    public String phoneNumber;
    public String profileImageUrl;
    public String role;
    public String status;
    public long createdAt;
}
