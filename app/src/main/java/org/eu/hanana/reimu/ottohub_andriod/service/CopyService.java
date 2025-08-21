package org.eu.hanana.reimu.ottohub_andriod.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.eu.hanana.reimu.ottohub_andriod.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CopyService extends Service {

    public static final String ACTION_PROGRESS = "org.eu.hanana.app.ottohub.copy.PROGRESS";
    public static final String ACTION_COMPLETE = "org.eu.hanana.app.ottohub.copy.COMPLETE";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_PATH = "path";

    private static final String CHANNEL_ID = "copy_channel";
    private static final AtomicInteger NEXT_NOTIFICATION_ID = new AtomicInteger(1002);

    private ExecutorService executor;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newFixedThreadPool(3); // 可以同时处理多个任务
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri contentUri = intent.getParcelableExtra("uri");
        String fileName = intent.getStringExtra("fileName");

        // 为每个任务生成独立通知 ID
        int notificationId = NEXT_NOTIFICATION_ID.getAndIncrement();

        // 启动前台服务（只需第一次启动）
        if (NEXT_NOTIFICATION_ID.get() == 1003) { // 第一个任务
            NotificationCompat.Builder builder = buildNotification(fileName, 0, true);
            startForeground(notificationId, builder.build());
        }

        executor.execute(() -> copyFile(contentUri, fileName, notificationId));
        return START_NOT_STICKY;
    }

    private void copyFile(Uri contentUri, String fileName, int notificationId) {
        try (InputStream inputStream = getContentResolver().openInputStream(contentUri)) {
            File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ottohub");
            if (!downloadDir.exists()) downloadDir.mkdirs();

            File outFile = new File(downloadDir, fileName);
            try (OutputStream outputStream = new FileOutputStream(outFile)) {

                byte[] buffer = new byte[8192];
                long total = 0;
                long fileSize = getFileSize(contentUri);

                int len;
                int lastProgress = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                    total += len;

                    if (fileSize > 0) {
                        int progress = (int) (total * 100 / fileSize);
                        if (progress != lastProgress) { // 限制刷新频率
                            sendProgress(progress);
                            updateNotification(progress, fileName, notificationId);
                            lastProgress = progress;
                        }
                    }
                }
            }

            // 扫描媒体库
            MediaScannerConnection.scanFile(this,
                    new String[]{outFile.getAbsolutePath()},
                    null, null);

            sendComplete(outFile.getAbsolutePath());
            showCompleteNotification(fileName, notificationId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getFileSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                return cursor.getLong(sizeIndex);
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private void sendProgress(int progress) {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.putExtra(EXTRA_PROGRESS, progress);
        sendBroadcast(intent);
    }

    private void sendComplete(String path) {
        Intent intent = new Intent(ACTION_COMPLETE);
        intent.putExtra(EXTRA_PATH, path);
        sendBroadcast(intent);
    }

    private void updateNotification(int progress, String fileName, int notificationId) {
        NotificationCompat.Builder builder = buildNotification(fileName, progress, false);
        notificationManager.notify(notificationId, builder.build());
    }

    private void showCompleteNotification(String fileName, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("复制完成")
                .setContentText(fileName + " 已保存到 Download/ottohub")
                .setSmallIcon(R.drawable.download_2_24dp)
                .setProgress(0, 0, false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify(notificationId, builder.build());
    }

    private NotificationCompat.Builder buildNotification(String fileName, int progress, boolean indeterminate) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("复制文件")
                .setContentText(fileName + (progress > 0 ? "  " + progress + "%" : ""))
                .setSmallIcon(R.drawable.download_2_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setProgress(100, progress, indeterminate);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, "文件复制", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
