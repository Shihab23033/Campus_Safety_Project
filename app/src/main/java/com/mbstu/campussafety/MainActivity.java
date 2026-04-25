package com.mbstu.campussafety;

import android.os.Bundle;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private String Password;
    private String Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Window Insets logic (keep your existing code here)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialize the views
        EditText userPassward = findViewById(R.id.userPassTxt);
        CheckBox showPass = findViewById(R.id.showPassCheck);

        // 2. Set the listener for the CheckBox
        showPass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show Password: Set type to visible text
                userPassward.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Hide Password: Set type back to textPassword
                userPassward.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }

            // 3. Move the cursor to the end of the text so it doesn't jump to the start
            userPassward.setSelection(userPassward.getText().length());
        });
    }
}