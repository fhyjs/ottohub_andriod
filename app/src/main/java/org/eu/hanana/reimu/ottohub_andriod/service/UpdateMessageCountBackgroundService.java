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
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

public class UpdateMessageCountBackgroundService extends Service {
    private Handler handler;
    private Runnable taskRunnable;
    private  long INTERVAL = 5 * 1000L; // 15分钟
    public static long INTERVAL_FOREGROUND = 5 * 1000L; // 15分钟
    public static long INTERVAL_BACKGROUND = 15 * 1000L; // 15分钟

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());

        taskRunnable = new Runnable() {
            @Override
            public void run() {
                // ✅ 在这里执行你的任务，比如获取邮件数量
                fetchUnreadMailCount();

                // 继续下一轮任务
                handler.postDelayed(this, INTERVAL);
            }
        };

        // 开启第一次执行
        handler.post(taskRunnable);
    }

    private void fetchUnreadMailCount() {
        Log.d("UpdateMessageCount", "Fetching unread mail count...");
        // 这里放你的网络请求代码，比如调用 ApiUtil.fetchMsgCount() 等
        Thread thread = new Thread(ApiUtil::fetchMsgCount);
        thread.setUncaughtExceptionHandler((t, e) -> {
            Notification error = new NotificationCompat.Builder(this, "error")
                    .setContentTitle(getString(R.string.ottohub))
                    .setContentText(getString(R.string.error) + e)
                    .setSmallIcon(R.drawable.ottoicon)
                    .build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1, error);
            }
        });
        thread.start();
        if (isAppForeground(this)){
            INTERVAL=INTERVAL_FOREGROUND;

        }else {
            INTERVAL=INTERVAL_BACKGROUND;
        }
    }
    public boolean isAppForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 显示前台通知（必须）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("force_background",true)) {
                startForeground(1, buildNotification());
            }else {
                //startForeground(1, buildNotification());
                //stopForeground(true);
                return START_NOT_STICKY; // 这里返回不重启服务
            }
        } else {
            // Android 8.0 以下，可启动前台服务后移除通知（实现“静默”）
            Notification notification = buildNotification();
            startForeground(1, notification);

            // 延迟几毫秒取消通知达到静默效果
            new Handler().postDelayed(() -> {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(1); // 取消通知
                stopForeground(false); // 保留服务状态，不再是“前台服务”
            }, 500); // 可根据设备调整
        }

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
                .setContentText("后台同步任务正在运行…")
                .setSmallIcon(R.drawable.ottoicon)
                .build();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 如果不支持绑定，可以返回 null
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyService", "服务被销毁");
    }
}
