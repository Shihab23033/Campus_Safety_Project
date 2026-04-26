package com.mbstu.campussafety.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mbstu.campussafety.models.EmergencyAlert;
import com.mbstu.campussafety.repository.EmergencyRepository;

import java.util.List;

public class EmergencyViewModel extends AndroidViewModel {
    private EmergencyRepository emergencyRepository;
    private MutableLiveData<EmergencyAlert> emergencyResponse;
    private MutableLiveData<List<EmergencyAlert>> emergencyAlerts;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public EmergencyViewModel(@NonNull Application application) {
        super(application);
        this.emergencyRepository = new EmergencyRepository(application);
        this.emergencyResponse = new MutableLiveData<>();
        this.emergencyAlerts = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }

    public MutableLiveData<EmergencyAlert> getEmergencyResponse() {
        return emergencyResponse;
    }

    public MutableLiveData<List<EmergencyAlert>> getEmergencyAlerts() {
        return emergencyAlerts;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void sendEmergencyAlert(EmergencyAlert alert) {
        isLoading.setValue(true);
        MutableLiveData<EmergencyAlert> response = emergencyRepository.sendEmergencyAlert(alert);
        response.observeForever(alert1 -> {
            emergencyResponse.setValue(alert1);
            isLoading.setValue(false);
            if (alert1 == null) {
                errorMessage.setValue("Failed to send emergency alert");
            }
        });
    }

    public void fetchEmergencyAlerts() {
        isLoading.setValue(true);
        MutableLiveData<List<EmergencyAlert>> response = emergencyRepository.getEmergencyAlerts();
        response.observeForever(alerts -> {
            emergencyAlerts.setValue(alerts);
            isLoading.setValue(false);
        });
    }

    public void fetchLocalEmergencyAlerts() {
        MutableLiveData<List<EmergencyAlert>> response = emergencyRepository.getLocalEmergencyAlerts();
        response.observeForever(alerts -> emergencyAlerts.setValue(alerts));
    }
}
