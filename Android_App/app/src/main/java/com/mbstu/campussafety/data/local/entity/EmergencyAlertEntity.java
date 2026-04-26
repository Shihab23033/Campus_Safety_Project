package com.mbstu.campussafety.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emergency_alerts")
public class EmergencyAlertEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String alertId;
    public String userId;
    public String emergencyType;
    public String description;
    public double latitude;
    public double longitude;
    public long timestamp;
    public String severity;
    public String status;
    public String audioUrl;
}
