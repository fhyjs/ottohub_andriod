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
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


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
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoCommentFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoDescribeFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.VlcMediaControl;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import yuku.ambilwarna.AmbilWarnaDialog;


public class VideoPlayerActivity extends AppCompatActivity {
    public static final String KEY_VID="vid";
    public static final String KEY_NET_DATA="netdata";
    public static final String KEY_DANMAKU_DATA="danmakudata";
    public static final String KEY_PLAYER_PLAYING="pplaying";
    public static final String KEY_PLAYER_TIME="ptime";
    private static final String TAG = "VideoPlayerActivity";
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoSurface;
    private DanmakuPlayer danmakuPlayer;
    private DanmakuConfig danmakuConfig;
    public int vid;
    private VideoResult netData;
    private DanmakuListResult danmakuData;
    protected Bundle savedInstanceState;
    private long lastDanmakuUpdate;
    public MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    .replace(R.id.fragment_container, VideoCommentFragment.newInstance(netData))
                    .commit();
        });
        findViewById(R.id.video_desc_btn).setEnabled(false);
        // 初始化 MediaController
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoSurface); // 设置锚点视图
        videoSurface.setOnClickListener(v -> {
            if(mediaController.isShowing()){
                mediaController.hide();
            }else {
                mediaController.show();
            }
        });
        setDanmakuEnable(true);
        init();
    }

    @Override
    protected void onPause() {
        mediaPlayer.detachViews();
        mediaController.hide();
        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        bindVideoSurface(videoSurface);
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
            outState.putLong(KEY_PLAYER_TIME,mediaPlayer.getTime());
        }
    }

    private void updateVideoScaling(int width, int height) {
        if (mediaPlayer==null) return;
        // 二次应用填充
        mediaPlayer.setScale(0);
        mediaPlayer.setAspectRatio("fill");
        // 特殊处理某些编解码器
        mediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_FILL);
    }
    public void init(){
        setTitle("loading...");
        initDanmaku();
        new Thread(()->{
            preinit();
            initPlayer();
            loadData(false);
            updateVideoScaling(videoSurface.getWidth(),videoSurface.getHeight());
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
                i = i & 0x00FFFFFF;
                int finalI = i;
                Thread thread = new Thread(() -> {
                    EmptyResult emptyResult = MyApp.getInstance().getOttohubApi().getDanmakuApi().send_danmaku(vid, input, mediaPlayer.getTime() / 1000d, "scroll", Integer.toHexString(finalI), "20px", "");
                    ApiUtil.throwApiError(emptyResult);
                    danmakuPlayer.send(new DanmakuItemData(danmakuPlayer.getCurrentTimeMs(),mediaPlayer.getTime()+10,input,DANMAKU_MODE_ROLLING,20,finalI,0,DANMAKU_STYLE_SELF_SEND,0,null,MERGED_TYPE_NORMAL));
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
        mediaController.hide();
        if (danmakuPlayer!=null){
            danmakuPlayer.stop();
            danmakuPlayer.release();
            danmakuPlayer=null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.getVLCVout().detachViews();
            mediaPlayer.release();
            libVLC.release();
            mediaPlayer=null;
        }
        // 释放资源
        if (mediaController != null) {
            mediaController.hide();
        }
    }
    public void bindVideoSurface(VLCVideoLayout videoSurface){
        if (mediaPlayer==null||mediaPlayer.getVLCVout().areViewsAttached()||videoSurface==null) {
            return;
        }
        mediaPlayer.attachViews(videoSurface, null, true, false);
        updateVideoScaling(videoSurface.getWidth(),videoSurface.getHeight());
    }
    private void initPlayer() {
        // 初始化VLC
        final ArrayList<String> args = new ArrayList<>();
        libVLC = new LibVLC(this, args);
        args.add("--vout=android-display");  // Add this line!
        args.add("-vvv");
        args.add(":avcodec-hw=any"); // 启用硬件加速
        args.add(":swscale-mode=0"); // 快速缩放模式
        args.add(":no-frame-drop"); // 避免丢帧
        // 创建媒体播放器
        mediaPlayer = new MediaPlayer(libVLC);
        runOnUiThread(()->{
            bindVideoSurface(videoSurface);
        });
        // 设置播放事件监听
        mediaPlayer.setEventListener(event -> {
            switch (event.type) {
                case MediaPlayer.Event.Playing:
                    if (isFinishing()) return;
                    danmakuPlayer.start(danmakuConfig);
                    danmakuPlayer.seekTo(event.getTimeChanged());
                    updateVideoScaling(videoSurface.getWidth(),videoSurface.getHeight());
                    mediaController.show();
                    break;
                case MediaPlayer.Event.Paused:
                    //danmakuPlayer.seekTo(event.getTimeChanged());
                    danmakuPlayer.pause();
                    break;
                case MediaPlayer.Event.EndReached:
                    danmakuPlayer.stop();
                    danmakuPlayer.pause();
                    mediaController.show();
                    mediaPlayer.stop();
                    break;
                case MediaPlayer.Event.TimeChanged:
                    lastDanmakuUpdate++;
                    if (lastDanmakuUpdate>7||Math.abs(danmakuPlayer.getCurrentTimeMs()-mediaPlayer.getTime())>70) {
                        danmakuPlayer.seekTo(mediaPlayer.getTime());
                        lastDanmakuUpdate=0;
                        Log.d(TAG, "updated danmaku time");
                    }
                    if (!mediaPlayer.isPlaying()) danmakuPlayer.pause();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    AlertUtil.showError(videoSurface.getContext(), "ERROR");
                    break;
            }
        });
        runOnUiThread(()->{
            var mc=new VlcMediaControl(mediaPlayer);
            mc.setSeeker(this::seekVideo);
            mediaController.setMediaPlayer(mc);
        });
    }
    public void seekVideo(long time){
        if (danmakuPlayer!=null) {
            danmakuPlayer.seekTo(time);
            if (mediaPlayer!=null&&!mediaPlayer.isPlaying()) danmakuPlayer.pause();
        }
        if (mediaPlayer!=null)
            mediaPlayer.setTime(time);
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
                setMedia(Uri.parse(netData.video_url));
                runOnUiThread(() -> {
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
            mediaPlayer.play();
            mediaPlayer.pause();
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

    public void setMedia(Uri uri){
        // 加载媒体
        Media media = new Media(libVLC,uri);
        media.setHWDecoderEnabled(true, false); // 启用硬件解码
        media.addOption(":fullscreen");
        media.addOption(":http-referrer=https://bilibili.com/");
        media.addOption(":http-user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Ottohub 1.0.0");
        mediaPlayer.setMedia(media);
        media.release();
    }
    private void initDanmaku() {
        danmakuPlayer = new DanmakuPlayer(new SimpleRenderer(),new DataSource());
        danmakuPlayer.bindView(findViewById(R.id.sv_danmaku));
        danmakuConfig = new DanmakuConfig();
        danmakuConfig.setAllowOverlap(true);
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