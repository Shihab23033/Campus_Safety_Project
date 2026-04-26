package com.mbstu.campussafety.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.mbstu.campussafety.data.local.CampusSafetyDatabase;
import com.mbstu.campussafety.data.local.entity.UserEntity;
import com.mbstu.campussafety.data.remote.ApiService;
import com.mbstu.campussafety.data.remote.RetrofitClient;
import com.mbstu.campussafety.models.AuthResponse;
import com.mbstu.campussafety.models.User;
import com.mbstu.campussafety.utils.SharedPreferenceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private SharedPreferenceManager prefManager;
    private CampusSafetyDatabase database;

    public AuthRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.prefManager = new SharedPreferenceManager(context);
        this.database = CampusSafetyDatabase.getInstance(context);
    }

    public MutableLiveData<AuthResponse> register(User user) {
        MutableLiveData<AuthResponse> result = new MutableLiveData<>();

        apiService.register(user).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        prefManager.saveToken(authResponse.getToken());
                        prefManager.setLoggedIn(true);
                        if (authResponse.getUser() != null) {
                            prefManager.saveUserId(authResponse.getUser().getUserId());
                            prefManager.saveEmail(authResponse.getUser().getEmail());
                        }
                    }
                    result.setValue(authResponse);
                } else {
                    AuthResponse error = new AuthResponse(false, "Registration failed", null, null);
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                AuthResponse error = new AuthResponse(false, t.getMessage(), null, null);
                result.setValue(error);
            }
        });

        return result;
    }

    public MutableLiveData<AuthResponse> login(String email, String password) {
        MutableLiveData<AuthResponse> result = new MutableLiveData<>();

        User credentials = new User();
        credentials.setEmail(email);

        apiService.login(credentials).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        prefManager.saveToken(authResponse.getToken());
                        prefManager.setLoggedIn(true);
                        if (authResponse.getUser() != null) {
                            prefManager.saveUserId(authResponse.getUser().getUserId());
                            prefManager.saveEmail(authResponse.getUser().getEmail());

                            // Save to local database
                            UserEntity userEntity = new UserEntity();
                            userEntity.userId = authResponse.getUser().getUserId();
                            userEntity.email = authResponse.getUser().getEmail();
                            userEntity.fullName = authResponse.getUser().getFullName();
                            userEntity.phoneNumber = authResponse.getUser().getPhoneNumber();
                            userEntity.role = authResponse.getUser().getRole();
                            database.userDao().insert(userEntity);
                        }
                    }
                    result.setValue(authResponse);
                } else {
                    AuthResponse error = new AuthResponse(false, "Login failed", null, null);
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                AuthResponse error = new AuthResponse(false, t.getMessage(), null, null);
                result.setValue(error);
            }
        });

        return result;
    }

    public void logout() {
        prefManager.clearAll();
    }

    public boolean isLoggedIn() {
        return prefManager.isLoggedIn();
    }

    public String getToken() {
        return prefManager.getToken();
    }

    public String getUserId() {
        return prefManager.getUserId();
    }
}
