package org.eu.hanana.reimu.ottohub_andriod.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.CrashActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CrashService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 必须先调用 startForeground，避免抛异常
        startForeground(10002, buildNotification(intent));

        Log.e("CS", "onStartCommand: App crashing service started!");
        var log = "crashed";
        try {
            log=readCrashFile(new File(Objects.requireNonNull(intent.getStringExtra("crash_path"))));
        }catch (Exception ignored){}
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bug_rep",true))
            sendCrashReport("https://hanana2.link/ottohub/app/report/bug_rp.php",log);
        return START_STICKY;
    }
    private String readCrashFile(File file) {
        if (file == null || !file.exists()) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            Log.e("CrashService", "读取日志文件失败", e);
        }
        return sb.toString();
    }

    private void sendCrashReport(String url, String jsonBody) {
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CrashService", "POST request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("CrashService", "POST request success: " + response.body().string());
                } else {
                    Log.w("CrashService", "POST request failed with code: " + response.code());
                }
            }
        });
    }
    private Notification buildNotification(Intent intent) {
        String channelId = "crash_channel";
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Crash Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("崩溃通知");
            channel.enableLights(true);
            channel.enableVibration(true);
            nm.createNotificationChannel(channel);
        }

        Intent crashIntent = new Intent(this, CrashActivity.class);
        crashIntent.putExtra("crash_path",intent.getStringExtra("crash_path"));
        crashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent crashPendingIntent = PendingIntent.getActivity(
                this, 0, crashIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("应用崩溃了")
                .setContentText("点击查看详情")
                .setSmallIcon(R.drawable.error_48px)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(crashPendingIntent)
                .setFullScreenIntent(crashPendingIntent, true) // 横幅
                .setAutoCancel(true)
                .build();
    }

}
