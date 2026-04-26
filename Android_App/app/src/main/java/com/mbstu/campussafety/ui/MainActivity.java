package com.mbstu.campussafety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.mbstu.campussafety.R;
import com.mbstu.campussafety.viewmodel.AuthViewModel;

public class MainActivity extends AppCompatActivity {
    private Button emergencyButton;
    private Button mapButton;
    private Button chatButton;
    private Button dashboardButton;
    private Button logoutButton;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize views
        emergencyButton = findViewById(R.id.emergencyButton);
        mapButton = findViewById(R.id.mapButton);
        chatButton = findViewById(R.id.chatButton);
        dashboardButton = findViewById(R.id.dashboardButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Emergency button click listener
        emergencyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencyAlertActivity.class);
            startActivity(intent);
        });

        // Map button click listener
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Chat button click listener
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // Dashboard button click listener
        dashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
            startActivity(intent);
        });

        // Logout button click listener
        logoutButton.setOnClickListener(v -> {
            authViewModel.logout();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}