package com.mbstu.campussafety.data.remote;

import com.mbstu.campussafety.models.AuthResponse;
import com.mbstu.campussafety.models.ChatMessage;
import com.mbstu.campussafety.models.EmergencyAlert;
import com.mbstu.campussafety.models.OTPRequest;
import com.mbstu.campussafety.models.OTPResponse;
import com.mbstu.campussafety.models.Responder;
import com.mbstu.campussafety.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // Authentication Endpoints
    @POST("auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("auth/login")
    Call<AuthResponse> login(@Body User credentials);

    @POST("auth/otp/send")
    Call<OTPResponse> sendOTP(@Body OTPRequest request);

    @POST("auth/otp/verify")
    Call<AuthResponse> verifyOTP(@Body OTPVerifyRequest request);

    @POST("auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body User user);

    @POST("auth/reset-password")
    Call<AuthResponse> resetPassword(
        @Header("Authorization") String token,
        @Body PasswordResetRequest request
    );

    // User Endpoints
    @GET("user/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    @PUT("user/profile")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Body User user);

    @POST("user/location")
    Call<Void> sendUserLocation(
        @Header("Authorization") String token,
        @Body LocationUpdate location
    );

    // Emergency Alert Endpoints
    @POST("emergency/alert")
    Call<EmergencyAlert> sendEmergencyAlert(
        @Header("Authorization") String token,
        @Body EmergencyAlert alert
    );

    @GET("emergency/alerts")
    Call<List<EmergencyAlert>> getEmergencyAlerts(@Header("Authorization") String token);

    @GET("emergency/alert/{alertId}")
    Call<EmergencyAlert> getAlertDetails(
        @Header("Authorization") String token,
        @Path("alertId") String alertId
    );

    @POST("emergency/alert/{alertId}/cancel")
    Call<Void> cancelAlert(
        @Header("Authorization") String token,
        @Path("alertId") String alertId
    );

    // Chat Endpoints
    @POST("chat/message")
    Call<ChatMessage> sendMessage(
        @Header("Authorization") String token,
        @Body ChatMessage message
    );

    @GET("chat/messages/{userId}")
    Call<List<ChatMessage>> getMessages(
        @Header("Authorization") String token,
        @Path("userId") String userId
    );

    // Responder Endpoints
    @GET("responder/nearby")
    Call<List<Responder>> getNearbyResponders(
        @Header("Authorization") String token,
        @Body LocationRequest location
    );

    @GET("responder/{responderId}")
    Call<Responder> getResponderDetails(
        @Header("Authorization") String token,
        @Path("responderId") String responderId
    );

    // WebSocket Endpoints (For real-time updates)
    // These would typically be handled separately via WebSocket connection
}

// Helper request/response classes
class OTPVerifyRequest {
    public String otpId;
    public String otpCode;

    public OTPVerifyRequest(String otpId, String otpCode) {
        this.otpId = otpId;
        this.otpCode = otpCode;
    }
}

class PasswordResetRequest {
    public String oldPassword;
    public String newPassword;

    public PasswordResetRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}

class LocationUpdate {
    public double latitude;
    public double longitude;
    public long timestamp;

    public LocationUpdate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }
}

class LocationRequest {
    public double latitude;
    public double longitude;
    public double radius;

    public LocationRequest(double latitude, double longitude, double radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }
}
