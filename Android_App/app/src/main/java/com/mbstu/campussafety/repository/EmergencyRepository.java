package com.mbstu.campussafety.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.mbstu.campussafety.data.local.CampusSafetyDatabase;
import com.mbstu.campussafety.data.local.entity.EmergencyAlertEntity;
import com.mbstu.campussafety.data.remote.ApiService;
import com.mbstu.campussafety.data.remote.RetrofitClient;
import com.mbstu.campussafety.models.EmergencyAlert;
import com.mbstu.campussafety.utils.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyRepository {
    private ApiService apiService;
    private SharedPreferenceManager prefManager;
    private CampusSafetyDatabase database;

    public EmergencyRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.prefManager = new SharedPreferenceManager(context);
        this.database = CampusSafetyDatabase.getInstance(context);
    }

    public MutableLiveData<EmergencyAlert> sendEmergencyAlert(EmergencyAlert alert) {
        MutableLiveData<EmergencyAlert> result = new MutableLiveData<>();
        String token = prefManager.getToken();

        apiService.sendEmergencyAlert("Bearer " + token, alert)
            .enqueue(new Callback<EmergencyAlert>() {
                @Override
                public void onResponse(Call<EmergencyAlert> call, Response<EmergencyAlert> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        EmergencyAlert responseAlert = response.body();

                        // Save to local database
                        EmergencyAlertEntity entity = new EmergencyAlertEntity();
                        entity.alertId = responseAlert.getAlertId();
                        entity.userId = responseAlert.getUserId();
                        entity.emergencyType = responseAlert.getEmergencyType();
                        entity.description = responseAlert.getDescription();
                        entity.latitude = responseAlert.getLatitude();
                        entity.longitude = responseAlert.getLongitude();
                        entity.timestamp = responseAlert.getTimestamp();
                        entity.severity = responseAlert.getSeverity();
                        entity.status = responseAlert.getStatus();
                        database.emergencyAlertDao().insert(entity);

                        result.setValue(responseAlert);
                    } else {
                        result.setValue(null);
                    }
                }

                @Override
                public void onFailure(Call<EmergencyAlert> call, Throwable t) {
                    result.setValue(null);
                }
            });

        return result;
    }

    public MutableLiveData<List<EmergencyAlert>> getEmergencyAlerts() {
        MutableLiveData<List<EmergencyAlert>> result = new MutableLiveData<>();
        String token = prefManager.getToken();

        apiService.getEmergencyAlerts("Bearer " + token)
            .enqueue(new Callback<List<EmergencyAlert>>() {
                @Override
                public void onResponse(Call<List<EmergencyAlert>> call, Response<List<EmergencyAlert>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        result.setValue(response.body());
                    } else {
                        result.setValue(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(Call<List<EmergencyAlert>> call, Throwable t) {
                    result.setValue(new ArrayList<>());
                }
            });

        return result;
    }

    public MutableLiveData<List<EmergencyAlert>> getLocalEmergencyAlerts() {
        MutableLiveData<List<EmergencyAlert>> result = new MutableLiveData<>();

        new Thread(() -> {
            List<EmergencyAlertEntity> entities = database.emergencyAlertDao().getAllAlerts();
            List<EmergencyAlert> alerts = new ArrayList<>();

            for (EmergencyAlertEntity entity : entities) {
                EmergencyAlert alert = new EmergencyAlert();
                alert.setAlertId(entity.alertId);
                alert.setUserId(entity.userId);
                alert.setEmergencyType(entity.emergencyType);
                alert.setDescription(entity.description);
                alert.setLatitude(entity.latitude);
                alert.setLongitude(entity.longitude);
                alert.setTimestamp(entity.timestamp);
                alert.setSeverity(entity.severity);
                alert.setStatus(entity.status);
                alerts.add(alert);
            }

            result.postValue(alerts);
        }).start();

        return result;
    }
}
