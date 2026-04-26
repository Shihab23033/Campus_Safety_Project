package com.mbstu.campussafety.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mbstu.campussafety.data.local.dao.ChatMessageDao;
import com.mbstu.campussafety.data.local.dao.EmergencyAlertDao;
import com.mbstu.campussafety.data.local.dao.UserDao;
import com.mbstu.campussafety.data.local.entity.ChatMessageEntity;
import com.mbstu.campussafety.data.local.entity.EmergencyAlertEntity;
import com.mbstu.campussafety.data.local.entity.UserEntity;

@Database(
    entities = {UserEntity.class, EmergencyAlertEntity.class, ChatMessageEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class CampusSafetyDatabase extends RoomDatabase {
    private static volatile CampusSafetyDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract EmergencyAlertDao emergencyAlertDao();
    public abstract ChatMessageDao chatMessageDao();

    public static CampusSafetyDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CampusSafetyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        CampusSafetyDatabase.class,
                        "campus_safety_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
