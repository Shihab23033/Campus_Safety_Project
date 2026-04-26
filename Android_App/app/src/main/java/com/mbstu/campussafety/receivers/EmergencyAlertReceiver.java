package com.mbstu.campussafety.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class EmergencyAlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle emergency alert broadcast
        String alertMessage = intent.getStringExtra("alert_message");
        Toast.makeText(context, alertMessage, Toast.LENGTH_LONG).show();

        // TODO: Handle emergency alert - start services, send notifications, etc.
    }
}
