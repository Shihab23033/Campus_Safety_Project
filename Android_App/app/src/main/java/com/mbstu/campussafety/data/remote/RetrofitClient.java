package com.mbstu.campussafety.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // For development: use local backend (need to replace with actual device IP or use emulator special IP)
    // private static final String BASE_URL = "http://10.0.2.2:8080/api/";  // For Android Emulator
    // private static final String BASE_URL = "http://localhost:8080/api/";  // For physical device on same network
    
    // For production: use deployed backend
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";  // Default to emulator
    
    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            okHttpClient = createOkHttpClient(context);
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getInstance(context).create(ApiService.class);
    }

    private static OkHttpClient createOkHttpClient(Context context) {
        return new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor(context))
            .build();
    }

    // Interceptor for adding JWT token to all requests
    static class AuthInterceptor implements Interceptor {
        private Context context;

        AuthInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
            Request originalRequest = chain.request();

            String token = getToken();
            Request.Builder requestBuilder = originalRequest.newBuilder();

            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }

        private String getToken() {
            try {
                MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

                SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "campus_safety_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                return sharedPreferences.getString("auth_token", null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
