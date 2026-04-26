package com.mbstu.campussafety.utils;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.mbstu.campussafety.workers.LocationUpdateWorker;

import java.util.concurrent.TimeUnit;

public class WorkManagerUtils {
    private static final String LOCATION_UPDATE_TAG = "location_update_work";

    public static void scheduleLocationUpdates(Context context) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build();

        PeriodicWorkRequest locationUpdateRequest = new PeriodicWorkRequest
            .Builder(LocationUpdateWorker.class, 15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(LOCATION_UPDATE_TAG)
            .build();

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                LOCATION_UPDATE_TAG,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                locationUpdateRequest
            );
    }

    public static void cancelLocationUpdates(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(LOCATION_UPDATE_TAG);
    }
}
