package com.mbstu.campussafety.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.mbstu.campussafety.R;
import com.mbstu.campussafety.viewmodel.EmergencyViewModel;

public class UserDashboardActivity extends AppCompatActivity {
    private ListView emergencyHistoryListView;
    private EmergencyViewModel emergencyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize ViewModel
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);

        // Initialize views
        emergencyHistoryListView = findViewById(R.id.emergencyHistoryListView);

        // Fetch emergency alerts
        emergencyViewModel.fetchLocalEmergencyAlerts();

        // Observe emergency alerts
        emergencyViewModel.getEmergencyAlerts().observe(this, alerts -> {
            if (alerts != null && !alerts.isEmpty()) {
                // TODO: Create adapter and bind to ListView
                Toast.makeText(UserDashboardActivity.this, "Alerts: " + alerts.size(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserDashboardActivity.this, "No emergency alerts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
