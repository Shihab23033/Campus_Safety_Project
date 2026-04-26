package com.mbstu.campussafety.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mbstu.campussafety.R;
import com.mbstu.campussafety.models.EmergencyAlert;
import com.mbstu.campussafety.services.AudioStreamingService;
import com.mbstu.campussafety.viewmodel.EmergencyViewModel;

public class EmergencyAlertActivity extends AppCompatActivity {
    private Button sosButton;
    private Spinner emergencyTypeSpinner;
    private EmergencyViewModel emergencyViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        // Initialize ViewModel
        emergencyViewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        sosButton = findViewById(R.id.sosButton);
        emergencyTypeSpinner = findViewById(R.id.emergencyTypeSpinner);

        // Check permissions
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
        }

        // SOS button click listener
        sosButton.setOnClickListener(v -> {
            sendEmergencyAlert();
        });

        // Observe emergency response
        emergencyViewModel.getEmergencyResponse().observe(this, alert -> {
            if (alert != null) {
                Toast.makeText(EmergencyAlertActivity.this, "Emergency alert sent!", Toast.LENGTH_SHORT).show();
                // Start audio streaming service
                Intent audioIntent = new Intent(EmergencyAlertActivity.this, AudioStreamingService.class);
                startForegroundService(audioIntent);
            }
        });
    }

    private void sendEmergencyAlert() {
        String emergencyType = emergencyTypeSpinner.getSelectedItem().toString();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    EmergencyAlert alert = new EmergencyAlert();
                    alert.setEmergencyType(emergencyType);
                    alert.setLatitude(location.getLatitude());
                    alert.setLongitude(location.getLongitude());
                    alert.setSeverity("HIGH");
                    alert.setDescription("Emergency: " + emergencyType);

                    emergencyViewModel.sendEmergencyAlert(alert);
                } else {
                    Toast.makeText(EmergencyAlertActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
