package org.eu.hanana.reimu.ottohub_andriod;

import android.app.Application;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.ottohub_andriod.service.UpdateMessageCountBackgroundService;

import lombok.Getter;

// MyApp.java
public class MyApp extends Application {
    @Getter
    private static MyApp instance;
    @Getter
    private OttohubApi ottohubApi;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ottohubApi=new OttohubApi();
        Intent serviceIntent = new Intent(this, UpdateMessageCountBackgroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

    }


}