package com.mbstu.campussafety.utils;

import android.util.Log;

import com.mbstu.campussafety.models.ChatMessage;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {
    private static final String TAG = "WebSocketClient";
    private static final String WS_URL = "wss://campus-safety-api.com/ws";

    private WebSocket webSocket;
    private OkHttpClient client;
    private WebSocketListener webSocketListener;

    public interface WebSocketCallback {
        void onMessageReceived(ChatMessage message);
        void onConnectionEstablished();
        void onConnectionClosed();
        void onError(String error);
    }

    public WebSocketClient(WebSocketCallback callback) {
        this.client = new OkHttpClient();
        this.webSocketListener = new okhttp3.WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket opened");
                callback.onConnectionEstablished();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Message received: " + text);
                // Parse JSON message and call callback
                callback.onMessageReceived(null); // TODO: Parse JSON to ChatMessage
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                callback.onConnectionClosed();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        };
    }

    public void connect(String token) {
        Request request = new Request.Builder()
            .url(WS_URL + "?token=" + token)
            .build();

        webSocket = client.newWebSocket(request, webSocketListener);
    }

    public void sendMessage(ChatMessage message) {
        if (webSocket != null) {
            // TODO: Convert message to JSON and send
            webSocket.send(message.getMessage());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Disconnect");
        }
    }
}
