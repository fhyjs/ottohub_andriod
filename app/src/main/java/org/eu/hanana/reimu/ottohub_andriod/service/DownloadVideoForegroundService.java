package org.eu.hanana.reimu.ottohub_andriod.service;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

public class DownloadVideoForegroundService extends Service {
    public static final String ARG_VID = "vid";
    private final IBinder binder = new LocalBinder();
    public int vid;
    public static Intent createIntent(int vid,Context context){
        Intent intent = new Intent(context, DownloadVideoForegroundService.class);
        intent.putExtra(ARG_VID,vid);
        return intent;
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, buildNotification());
        return START_STICKY;
    }
    private Notification buildNotification() {
        String channelId = "sync_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "同步任务", NotificationManager.IMPORTANCE_MIN);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.enableLights(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("同步进行中")
                .setContentText(getText(R.string.downloading))
                .setSmallIcon(R.drawable.ottoicon)
                .build();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder; // 如果不支持绑定，可以返回 null
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyService", "服务被销毁");
    }
    public class LocalBinder extends Binder {
        public DownloadVideoForegroundService getService() {
            return DownloadVideoForegroundService.this;
        }
    }
}
