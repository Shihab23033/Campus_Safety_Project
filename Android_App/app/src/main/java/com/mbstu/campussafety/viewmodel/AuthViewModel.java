package com.mbstu.campussafety.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mbstu.campussafety.models.AuthResponse;
import com.mbstu.campussafety.models.User;
import com.mbstu.campussafety.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<AuthResponse> authResponse;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
        this.authResponse = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }

    public MutableLiveData<AuthResponse> getAuthResponse() {
        return authResponse;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void register(User user) {
        isLoading.setValue(true);
        MutableLiveData<AuthResponse> response = authRepository.register(user);
        response.observeForever(authResponse -> {
            this.authResponse.setValue(authResponse);
            isLoading.setValue(false);
            if (!authResponse.isSuccess()) {
                errorMessage.setValue(authResponse.getMessage());
            }
        });
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        MutableLiveData<AuthResponse> response = authRepository.login(email, password);
        response.observeForever(authResponse -> {
            this.authResponse.setValue(authResponse);
            isLoading.setValue(false);
            if (!authResponse.isSuccess()) {
                errorMessage.setValue(authResponse.getMessage());
            }
        });
    }

    public void logout() {
        authRepository.logout();
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public String getToken() {
        return authRepository.getToken();
    }

    public String getUserId() {
        return authRepository.getUserId();
    }
}
