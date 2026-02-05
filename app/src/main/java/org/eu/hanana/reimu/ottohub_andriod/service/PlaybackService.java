package org.eu.hanana.reimu.ottohub_andriod.service;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.ui.PlayerNotificationManager;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;

import org.eu.hanana.reimu.ottohub_andriod.R;


import lombok.Getter;

@UnstableApi
public class PlaybackService extends MediaSessionService {
    // PlaybackService 内定义
    private static final int NOTIFICATION_ID = 1003;           // 通知 ID，任意正整数即可
    private static final String CHANNEL_ID = "playback_channel"; // 通知渠道 ID，必须唯一
    public static final String BIND_LOCAL = "org.eu.hanana.reimu.BIND_LOCAL";
    @Getter
    private ExoPlayer player;
    private MediaSession mediaSession;
    private PlayerNotificationManager playerNotificationManager;
    @Getter
    protected boolean alive;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private void showFloatingWindow() {
        if (floatingView != null) return; // 已经显示

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_player, null);

        // 设置初始布局参数
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                (int) (windowManager.getDefaultDisplay().getWidth()*0.8f), (int) (windowManager.getDefaultDisplay().getHeight()*0.3f),  // 初始宽高
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        windowManager.addView(floatingView, params);

        // 初始化 ExoPlayer 到悬浮窗
        PlayerView playerView = floatingView.findViewById(R.id.floating_player_view);
        playerView.setPlayer(player);

        enableDragAndResize(playerView);
    }
    private void enableDragAndResize(PlayerView cv) {
        cv.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            private int originalWidth, originalHeight;
            private float touchStartX, touchStartY;
            private boolean isResizing = false;
            private final int RESIZE_MARGIN = 40; // 右下角拖拽区域

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = params.x;
                        lastY = params.y;
                        touchStartX = event.getRawX();
                        touchStartY = event.getRawY();
                        originalWidth = params.width;
                        originalHeight = params.height;

                        // 判断是否点击右下角调整大小
                        isResizing = (event.getX() > v.getWidth() - RESIZE_MARGIN) &&
                                (event.getY() > v.getHeight() - RESIZE_MARGIN);
                        if (cv.isControllerFullyVisible()) {
                            cv.hideController();
                        }else {
                            cv.showController();
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (isResizing) {
                            params.width = (int) Math.max(200, originalWidth + (rawX - touchStartX));
                            params.height = (int) Math.max(120, originalHeight + (rawY - touchStartY));
                        } else {
                            params.x = (int) (lastX + (rawX - touchStartX));
                            params.y = (int) (lastY + (rawY - touchStartY));
                        }
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        isResizing = false;
                        return true;
                }
                return false;
            }
        });
    }

    private void removeFloatingWindow() {
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();
        alive=true;

        // 1️⃣ 创建 ExoPlayer
        player = new ExoPlayer.Builder(this).build();

        // 2️⃣ 创建 MediaSession
        mediaSession = new MediaSession.Builder(this, player)
                .build();

        // 3️⃣ 创建 PlayerNotificationManager（媒体通知）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Video Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Video playback controls");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
         playerNotificationManager =
                new PlayerNotificationManager.Builder(
                        this,
                        NOTIFICATION_ID,
                        CHANNEL_ID
                ).setMediaDescriptionAdapter(new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        CharSequence title = player.getCurrentMediaItem() != null
                                ? player.getCurrentMediaItem().mediaMetadata.title
                                : "未知视频";
                        return title != null ? title.toString() : "未知视频";
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {
                        CharSequence subtitle = player.getCurrentMediaItem() != null
                                ? player.getCurrentMediaItem().mediaMetadata.artist
                                : null;
                        return subtitle != null ? subtitle.toString() : null;
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        if (player.getCurrentMediaItem() == null) return null;

                        Uri artworkUri = player.getCurrentMediaItem().mediaMetadata.artworkUri;
                        if (artworkUri == null) return null;

                        // 异步加载图片（这里用 Glide 示例，也可以用你自己的图片加载方式）
                        new Thread(() -> {
                            try {
                                Bitmap bitmap = Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(artworkUri)
                                        .submit()
                                        .get();
                                callback.onBitmap(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();

                        return null; // 返回 null 表示暂时没有封面，加载完成后 callback 会更新
                    }
                }).setNotificationListener(new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
                    }

                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                        PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
                        //启动前台服务
                        // 保证 Service 前台
                        startForeground(notificationId, notification);
                    }
                })
                        .setSmallIconResourceId(R.drawable.ottoicon)
                        .build();

        // 绑定 Player
        playerNotificationManager.setPlayer(player);
        Log.d("PlaybackService", "onCreate called");
    }

    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }
    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d("PlaybackService", "onStartCommand called");
        alive=true;
        if (intent != null && "SHOW_FLOATING".equals(intent.getAction())) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent1 = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                ).addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);

            }else {
                showFloatingWindow();
            }
        }if (intent.getAction().equals(BIND_LOCAL)){
            removeFloatingWindow();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("PlaybackService", "onBind called");
        IBinder superBinder = super.onBind(intent);
        if (superBinder != null) {
            return superBinder;
        }

        if (BIND_LOCAL.equals(intent.getAction())) {
            return binder;
        }
        return null;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onDestroy() {
        alive=false;
        if (floatingView!=null) removeFloatingWindow();
        Log.d("PlaybackService", "onDestroy called");
        mediaSession.release();
        player.release();
        playerNotificationManager.setPlayer(null);
        // 停止前台服务并移除通知
        stopForeground(true);

        super.onDestroy();
    }
}
