package com.mbstu.campussafety.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mbstu.campussafety.utils.SharedPreferenceManager;

public class LocationUpdateWorker extends Worker {
    private static final String TAG = "LocationUpdateWorker";

    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Get the current location from SharedPreferences
            SharedPreferenceManager prefManager = new SharedPreferenceManager(getApplicationContext());
            double[] location = prefManager.getLocation();

            Log.d(TAG, "Sending location update: " + location[0] + ", " + location[1]);

            // TODO: Send location to backend API
            // Uncomment when API is ready:
            // ApiService apiService = RetrofitClient.getApiService(getApplicationContext());
            // LocationUpdate update = new LocationUpdate(location[0], location[1]);
            // Call<Void> call = apiService.sendUserLocation("Bearer " + prefManager.getToken(), update);
            // Response<Void> response = call.execute();
            // if (response.isSuccessful()) {
            //     return Result.success();
            // }

            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Error updating location", e);
            return Result.retry();
        }
    }
}
