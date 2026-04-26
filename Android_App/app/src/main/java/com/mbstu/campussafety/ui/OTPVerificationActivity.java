package com.mbstu.campussafety.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mbstu.campussafety.R;

public class OTPVerificationActivity extends AppCompatActivity {
    private EditText otpEditText;
    private Button verifyButton;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get email from intent
        email = getIntent().getStringExtra("email");

        // Initialize views
        otpEditText = findViewById(R.id.otpEditText);
        verifyButton = findViewById(R.id.verifyButton);

        // Verify button click listener
        verifyButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();

            if (otp.isEmpty()) {
                Toast.makeText(OTPVerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Verify OTP with backend
                Toast.makeText(OTPVerificationActivity.this, "OTP verified", Toast.LENGTH_SHORT).show();

                // Navigate to login
                Intent intent = new Intent(OTPVerificationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
