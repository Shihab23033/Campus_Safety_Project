package com.mbstu.campussafety.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mbstu.campussafety.R;

public class ChatActivity extends AppCompatActivity {
    private EditText messageEditText;
    private Button sendButton;
    private ListView chatListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatListView = findViewById(R.id.chatListView);

        // Send button click listener
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Send message via WebSocket
                messageEditText.setText("");
                Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
