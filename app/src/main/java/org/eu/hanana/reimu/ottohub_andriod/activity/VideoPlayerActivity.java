package org.eu.hanana.reimu.ottohub_andriod.activity;

import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_CENTER_TOP;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_ROLLING;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_STYLE_NONE;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_STYLE_SELF_SEND;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.MERGED_TYPE_NORMAL;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.kuaishou.akdanmaku.DanmakuConfig;
import com.kuaishou.akdanmaku.data.DanmakuItemData;
import com.kuaishou.akdanmaku.data.DataSource;
import com.kuaishou.akdanmaku.render.SimpleRenderer;
import com.kuaishou.akdanmaku.ui.DanmakuPlayer;

import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.danmaku.DanmakuListResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoDescribeFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import yuku.ambilwarna.AmbilWarnaDialog;


@UnstableApi
public class VideoPlayerActivity extends AppCompatActivity {
    public static final String KEY_VID="vid";
    public static final String KEY_NET_DATA="netdata";
    public static final String KEY_DANMAKU_DATA="danmakudata";
    public static final String KEY_PLAYER_PLAYING="pplaying";
    public static final String KEY_PLAYER_TIME="ptime";
    private static final String TAG = "VideoPlayerActivity";
    private ExoPlayer mediaPlayer;
    private PlayerView videoSurface;
    private DanmakuPlayer danmakuPlayer;
    private DanmakuConfig danmakuConfig;
    public int vid;
    private VideoResult netData;
    private DanmakuListResult danmakuData;
    protected Bundle savedInstanceState;
    private long lastDanmakuUpdate;
    private Runnable updateRunnable;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(getMainLooper());
        this.savedInstanceState=savedInstanceState;
        if (savedInstanceState==null){
            this.savedInstanceState=new Bundle();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        vid = getIntent().getExtras().getInt(KEY_VID);
        videoSurface = findViewById(R.id.video_view);
        findViewById(R.id.video_desc_btn).setOnClickListener(v -> {
            v.setEnabled(false);
            findViewById(R.id.video_comment_btn).setEnabled(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, VideoDescribeFragment.newInstance(netData))
                    .commit();
        });
        findViewById(R.id.video_comment_btn).setOnClickListener(v -> {
            v.setEnabled(false);
            findViewById(R.id.video_desc_btn).setEnabled(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, CommentFragmentBase.newInstance(netData.vid,CommentFragmentBase.TYPE_VIDEO))
                    .commit();
        });
        findViewById(R.id.video_desc_btn).setEnabled(false);
        setDanmakuEnable(true);
        init();
    }

    @Override
    protected void onPause() {

        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setDanmakuEnable(boolean danmakuEnable) {
        findViewById(R.id.sv_danmaku).setVisibility(danmakuEnable?View.VISIBLE:View.INVISIBLE);
        ((MaterialButton) findViewById(R.id.btnDanmaku)).setIcon(AppCompatResources.getDrawable(this,danmakuEnable?R.drawable.chat_24dp_fill:R.drawable.chat_24px));
    }
    public boolean getDanmakuEnable() {
        return findViewById(R.id.sv_danmaku).getVisibility()==View.VISIBLE;
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        var gson = new Gson();
        outState.putString(KEY_NET_DATA,gson.toJson(netData));
        outState.putString(KEY_DANMAKU_DATA,gson.toJson(danmakuData));
        if (mediaPlayer!=null){
            outState.putBoolean(KEY_PLAYER_PLAYING,mediaPlayer.isPlaying());
            outState.putLong(KEY_PLAYER_TIME,mediaPlayer.getCurrentPosition());
        }
    }

    public void init(){
        setTitle("loading...");
        initDanmaku();
        new Thread(()->{
            preinit();
            initPlayer();
            loadData(false);
            postinit();
        }).start();
    }

    private void postinit() {
        findViewById(R.id.btnDanmaku).setOnClickListener(v -> {
            setDanmakuEnable(!getDanmakuEnable());
        });
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            AtomicInteger atomicInteger = new AtomicInteger(0xffffffff);
            BottomSheetDialog bottomSheetDialog = AlertUtil.showInput(this, input -> {
                int i = atomicInteger.get();
                String color = Integer.toHexString(i).substring(2);
                Thread thread = new Thread(() -> {
                    EmptyResult emptyResult = MyApp.getInstance().getOttohubApi().getDanmakuApi().send_danmaku(vid, input, mediaPlayer.getCurrentPosition() / 1000d, "scroll",color, "20px", "");

                    ApiUtil.throwApiError(emptyResult);
                    danmakuPlayer.send(new DanmakuItemData(danmakuPlayer.getCurrentTimeMs(),mediaPlayer.getCurrentPosition()+10,input,DANMAKU_MODE_ROLLING,20,Color.parseColor("#"+color),0,DANMAKU_STYLE_SELF_SEND,0,null,MERGED_TYPE_NORMAL));
                });
                thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
                thread.start();
            });
            EditText editInput = bottomSheetDialog.findViewById(R.id.edit_input);
            LinearLayout rootView = (LinearLayout) editInput.getParent();
            rootView.removeView(editInput);
            LinearLayout linearLayout = new LinearLayout(bottomSheetDialog.getContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.addView(editInput);
            MaterialButton colorBtn = new MaterialButton(bottomSheetDialog.getContext());
            colorBtn.setIcon(AppCompatResources.getDrawable(this,R.drawable.border_color));
            colorBtn.setPadding(0,0,5,0);
            colorBtn.setMinWidth(0);
            linearLayout.addView(colorBtn,0);
            colorBtn.setOnClickListener(v1 -> {
                editInput.clearFocus();
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, editInput.getCurrentTextColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        editInput.setTextColor(color);
                        colorBtn.setIconTint(ColorStateList.valueOf(color));
                        atomicInteger.set(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });
                dialog.show();
            });
            rootView.addView(linearLayout,0);
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            bottomSheetDialog.show();
        });
    }

    private void preinit() {

    }

    public void  destroy(){
        stopProgressUpdater();
        if (danmakuPlayer!=null){
            danmakuPlayer.stop();
            danmakuPlayer.release();
            danmakuPlayer=null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initPlayer() {
        // 创建媒体播放器
        mediaPlayer = new ExoPlayer.Builder(this).build();
        runOnUiThread(()->{
            videoSurface.setPlayer(mediaPlayer);
        });

        mediaPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying){
                    if (isFinishing()) return;
                    danmakuPlayer.start(danmakuConfig);
                    danmakuPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    //updateVideoScaling(videoSurface.getWidth(),videoSurface.getHeight());
                }else {
                    danmakuPlayer.pause();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState){
                    case Player.STATE_ENDED:
                        danmakuPlayer.stop();
                        danmakuPlayer.pause();
                        mediaPlayer.stop();
                        break;
                    case Player.STATE_BUFFERING:
                        break;
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_READY:
                        break;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                AlertUtil.showError(videoSurface.getContext(), "ERROR:"+error);
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
                danmakuPlayer.seekTo(newPosition.positionMs);
                internalTimeCheck();
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Player.Listener.super.onPlaybackParametersChanged(playbackParameters);
                danmakuPlayer.updatePlaySpeed(playbackParameters.speed);
            }
        });
        startProgressUpdater();
    }
    public void internalTimeCheck(){
        lastDanmakuUpdate++;
        if (lastDanmakuUpdate>5||Math.abs(danmakuPlayer.getCurrentTimeMs()-mediaPlayer.getCurrentPosition())>70) {
            danmakuPlayer.seekTo(mediaPlayer.getCurrentPosition());
            lastDanmakuUpdate=0;
            Log.d(TAG, "updated danmaku time");
        }
        if (!mediaPlayer.isPlaying()) danmakuPlayer.pause();
    }
    public void seekVideo(long time){
        if (danmakuPlayer!=null) {
            danmakuPlayer.seekTo(time);
            if (mediaPlayer!=null&&!mediaPlayer.isPlaying()) danmakuPlayer.pause();
        }
        if (mediaPlayer!=null)
            mediaPlayer.seekTo(time);
    }
    private void loadData(boolean finish) {
        if (!finish) {
            Thread thread = new Thread(() -> {
                var gson = new Gson();
                if (!savedInstanceState.containsKey(KEY_NET_DATA)) {
                    netData = MyApp.getInstance().getOttohubApi().getVideoApi().get_video_detail(vid);
                }else {
                    netData = gson.fromJson(savedInstanceState.getString(KEY_NET_DATA),VideoResult.class);
                    Log.d(TAG, "loadData: net data from storage");
                }
                if (!savedInstanceState.containsKey(KEY_DANMAKU_DATA)) {
                    danmakuData = MyApp.getInstance().getOttohubApi().getDanmakuApi().get_danmaku(vid);
                }else {
                    danmakuData = gson.fromJson(savedInstanceState.getString(KEY_DANMAKU_DATA),DanmakuListResult.class);
                    Log.d(TAG, "loadData: danmaku data from storage");
                }
                if (!netData.isSuccess() || !danmakuData.isSuccess()) {
                    runOnUiThread(()->{
                        AlertUtil.showError(videoSurface.getContext(), "ERROR" + netData.getMessage());
                    });
                    return;
                }
                loadDanmaku();
                runOnUiThread(() -> {
                    setMedia(Uri.parse(netData.video_url));
                    loadData(true);
                });
            });
            thread.setUncaughtExceptionHandler((t, e) -> AlertUtil.showError(videoSurface.getContext(), "ERROR:"+ e));
            thread.start();
        }else {
            System.out.println(netData.title);
            setTitle(netData.title);
            // 默认加载第一个 Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, VideoDescribeFragment.newInstance(netData))
                    .commit();
            mediaPlayer.prepare();
            if (savedInstanceState.containsKey(KEY_PLAYER_TIME)){
                long time = savedInstanceState.getLong(KEY_PLAYER_TIME);
                if (time>0) seekVideo(time);
            }
            if (savedInstanceState.containsKey(KEY_PLAYER_PLAYING)){
                if (savedInstanceState.getBoolean(KEY_PLAYER_PLAYING)){
                    mediaPlayer.play();
                }else {
                    mediaPlayer.pause();
                }
            }else {
                mediaPlayer.play();
            }
        }
    }

    private void loadDanmaku() {
        for (int i = 0; i < danmakuData.data.size(); i++) {
            var data = danmakuData.data.get(i);
            var mode = 0;
            if (data.mode.equals("scroll")){
                mode=DANMAKU_MODE_ROLLING;
            }else if (data.mode.equals("top")){
                mode=DANMAKU_MODE_CENTER_TOP;
            }else if (data.mode.equals("bottom")){
                mode=DANMAKU_MODE_CENTER_BOTTOM;
            }else{
                Log.w(getClass().getName(),"Unknown danmaku type: "+data.mode);
            }
            danmakuPlayer.send(new DanmakuItemData(
                    i,
                    (long)(data.time*1000),
                    data.text,
                    mode,
                    Integer.parseInt(data.font_size.substring(0,data.font_size.length()-2)),
                    Color.parseColor(data.color),
                    0,
                    DANMAKU_STYLE_NONE,
                    0,
                    null,
                    MERGED_TYPE_NORMAL
            ));
            Log.d(TAG, "loadDanmaku: Color.parseColor(danmakuData.data.get(0).color): "+ Color.parseColor(data.color)+" raw: "+data.color);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setMedia(Uri uri){
        // 加载媒体
        MediaItem mediaItem = MediaItem.fromUri(uri);
        // 设置 Referer 头
        HttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setDefaultRequestProperties(
                                Map.of("Referer", "https://bilibili.com/")
                        ).setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Ottohub 1.0.0");
        // 使用 MediaSource 时注入 DataSource
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(httpDataSourceFactory)
                .createMediaSource(mediaItem);
        mediaPlayer.setMediaSource(mediaSource);
    }
    private void initDanmaku() {
        danmakuPlayer = new DanmakuPlayer(new SimpleRenderer(),new DataSource());
        danmakuPlayer.bindView(findViewById(R.id.sv_danmaku));
        danmakuConfig = new DanmakuConfig();
        danmakuConfig.setAllowOverlap(true);
    }
    private void startProgressUpdater() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    long position = mediaPlayer.getCurrentPosition(); // 当前播放进度（毫秒）
                    long duration = mediaPlayer.getDuration();        // 总时长

                    // 执行你想做的事情，比如更新 TextView、发送数据、打印日志
                    internalTimeCheck();
                }
                handler.postDelayed(this, 500); // 每隔 1 秒再执行
            }
        };

        handler.post(updateRunnable); // 启动定时任务
    }

    private void stopProgressUpdater() {
        handler.removeCallbacks(updateRunnable); // 停止任务
    }
    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        // 或使用更精确的比例控制（推荐）
        //videoSurface.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            // 获取 SurfaceView 尺寸

        //});
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = super.onCreateView(parent, name, context, attrs);

        return view;
    }

    @Override
    protected void onDestroy() {
        destroy();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 默认返回栈顶页面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}