package org.eu.hanana.reimu.ottohub_andriod.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import org.eu.hanana.reimu.ottohub_andriod.service.UpdateMessageCountBackgroundService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, UpdateMessageCountBackgroundService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}