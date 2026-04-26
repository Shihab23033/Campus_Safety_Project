package com.mbstu.campussafety.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mbstu.campussafety.data.local.entity.EmergencyAlertEntity;

import java.util.List;

@Dao
public interface EmergencyAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EmergencyAlertEntity alert);

    @Update
    void update(EmergencyAlertEntity alert);

    @Delete
    void delete(EmergencyAlertEntity alert);

    @Query("SELECT * FROM emergency_alerts WHERE alertId = :alertId")
    EmergencyAlertEntity getAlertById(String alertId);

    @Query("SELECT * FROM emergency_alerts ORDER BY timestamp DESC")
    List<EmergencyAlertEntity> getAllAlerts();

    @Query("SELECT * FROM emergency_alerts WHERE userId = :userId ORDER BY timestamp DESC")
    List<EmergencyAlertEntity> getUserAlerts(String userId);

    @Query("DELETE FROM emergency_alerts")
    void deleteAllAlerts();
}
