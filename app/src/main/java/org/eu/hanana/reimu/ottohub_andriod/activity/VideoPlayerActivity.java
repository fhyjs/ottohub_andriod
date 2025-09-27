package org.eu.hanana.reimu.ottohub_andriod.activity;

import static android.view.View.GONE;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_CENTER_TOP;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_ROLLING;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_STYLE_NONE;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_STYLE_SELF_SEND;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.MERGED_TYPE_NORMAL;


import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_AUDIT;
import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_VIEW;
import static org.eu.hanana.reimu.ottohub_andriod.util.UiUtil.getScaleTypeVideoInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.kuaishou.akdanmaku.DanmakuConfig;
import com.kuaishou.akdanmaku.data.DanmakuItem;
import com.kuaishou.akdanmaku.data.DanmakuItemData;
import com.kuaishou.akdanmaku.data.DataSource;
import com.kuaishou.akdanmaku.ui.DanmakuPlayer;
import com.kuaishou.akdanmaku.ui.DanmakuView;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.danmaku.DanmakuListResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditVideoFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.FragmentFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.UnlockedDanmakuRender;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoDescribeFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.DynamicFragmentAdapter;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import yuku.ambilwarna.AmbilWarnaDialog;


@UnstableApi
public class VideoPlayerActivity extends BaseActivity {
    public static final String KEY_VID="vid";
    public static final String KEY_NET_DATA="netdata";
    public static final String KEY_DANMAKU_DATA="danmakudata";
    public static final String KEY_PLAYER_PLAYING="pplaying";
    public static final String KEY_PLAYER_TIME="ptime";
    public static final String KEY_TYPE="type";
    public static final String KEY_DATA="data";
    private static final String TAG = "VideoPlayerActivity";
    private ExoPlayer mediaPlayer;
    private PlayerView videoSurface;
    private DanmakuView danmakuView;
    private DanmakuPlayer danmakuPlayer;
    private DanmakuConfig danmakuConfig;
    public int vid;
    private VideoResult netData;
    private DanmakuListResult danmakuData;
    protected Bundle savedInstanceState;
    private long lastDanmakuUpdate;
    private Runnable updateRunnable;
    private Handler handler;
    public String type = TYPE_VIEW;
    @Nullable
    public String data = null;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    public DynamicFragmentAdapter dynamicFragmentAdapter;

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
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           // return insets;
        //});
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        vid = getIntent().getExtras().getInt(KEY_VID);
        videoSurface = findViewById(R.id.video_view);
        if (getIntent().getExtras().containsKey(KEY_DATA)){
            data=getIntent().getExtras().getString(KEY_DATA);
        }
        if (getIntent().getExtras().containsKey(KEY_TYPE)){
            type=getIntent().getExtras().getString(KEY_TYPE);
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager.setAdapter(dynamicFragmentAdapter=new DynamicFragmentAdapter(this));
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        Map<Fragment,Integer>  fragHeight= new HashMap<>();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Fragment fragment = dynamicFragmentAdapter.createFragment(position);
                if (fragment instanceof FragmentFragment){
                    viewPager.setLayoutParams(new LinearLayout.LayoutParams(viewPager.getWidth(),UiUtil.getAppWindowHeight(VideoPlayerActivity.this)-findViewById(R.id.horizontalScrollView).getHeight()));
                    return;
                }
                var view = fragment.getView();
                if (fragHeight.containsKey(fragment)){
                    ViewGroup.LayoutParams lp = viewPager.getLayoutParams();
                    lp.height = fragHeight.get(fragment);
                    viewPager.setLayoutParams(lp);
                    return;
                }
                if (view != null) {
                    view.post(() -> {

                        int wSpec = View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY);
                        int hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        view.measure(wSpec, hSpec);

                        ViewGroup.LayoutParams lp = viewPager.getLayoutParams();
                        lp.height = view.getMeasuredHeight();
                        viewPager.setLayoutParams(lp);
                        fragHeight.put(fragment,view.getMeasuredHeight());
                    });
                }
            }
        });
        // 将 TabLayout 与 ViewPager2 绑定
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position<dynamicFragmentAdapter.getItemCount()){
                        tab.setText(dynamicFragmentAdapter.getTitle(position));
                    }
                }).attach();


        setDanmakuEnable(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        init();
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                         View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        // 获取控制器
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // 隐藏状态栏和导航栏
        controller.hide(WindowInsetsCompat.Type.systemBars());

        // 设置行为：允许手势临时呼出系统栏，几秒后自动隐藏
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }
    protected boolean lastPlayStatus;
    @Override
    protected void onPause() {
        lastPlayStatus=mediaPlayer.isPlaying();
        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideNavigationBar();
        }
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
            outState.putBoolean(KEY_PLAYER_PLAYING,mediaPlayer.isPlaying()||lastPlayStatus);
            outState.putLong(KEY_PLAYER_TIME,mediaPlayer.getCurrentPosition());
        }
    }

    public void init(){
        setTitle("loading...");
        initDanmaku();
        Thread thread = new Thread(() -> {
            preinit();
            initPlayer();
            if (mediaPlayer == null) return;
            ;
            loadData(false);
            if (mediaPlayer == null) return;
            ;
            postinit();
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
        thread.start();
    }


    private void postinit() {
        findViewById(R.id.btnDanmaku).setOnClickListener(v -> {
            setDanmakuEnable(!getDanmakuEnable());
        });
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            AtomicInteger atomicInteger = new AtomicInteger(0xffffffff);
            AtomicInteger atomicTextSize = new AtomicInteger(20);
            AtomicReference<String> atomicDanmakuType = new AtomicReference<>("scroll");
            BottomSheetDialog bottomSheetDialog = AlertUtil.showInput(this, input -> {
                int i = atomicInteger.get();
                String color = Integer.toHexString(i).substring(2);
                var time = mediaPlayer.getCurrentPosition();
                Thread thread = new Thread(() -> {
                    EmptyResult emptyResult = MyApp.getInstance().getOttohubApi().getDanmakuApi().send_danmaku(vid, input,  time/ 1000d, atomicDanmakuType.get(),color, atomicTextSize.get() +"px", "");
                    ApiUtil.throwApiError(emptyResult);
                    var local_type=-1;
                    if (atomicDanmakuType.get().equals("scroll")){
                        local_type=DANMAKU_MODE_ROLLING;
                    }else if (atomicDanmakuType.get().equals("top")){
                        local_type=DANMAKU_MODE_CENTER_TOP;
                    }else if (atomicDanmakuType.get().equals("bottom")){
                        local_type=DANMAKU_MODE_CENTER_BOTTOM;
                    }
                    danmakuPlayer.send(new DanmakuItemData(danmakuPlayer.getCurrentTimeMs(),time+10,input,local_type,atomicTextSize.get(),Color.parseColor("#"+color),0,DANMAKU_STYLE_SELF_SEND,0,null,MERGED_TYPE_NORMAL));
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
            ImageButton colorBtn = new ImageButton(bottomSheetDialog.getContext());
            ImageButton styleBtn = new ImageButton(bottomSheetDialog.getContext());
            colorBtn.setBackground(null);
            styleBtn.setBackground(null);
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.border_color);
            Drawable drawableStyle = AppCompatResources.getDrawable(this, R.drawable.style_24dp);
            drawable.setTint(ThemeUtil.getTheme(this).getColorPrimary());
            drawableStyle.setTint(ThemeUtil.getTheme(this).getColorPrimary());
            colorBtn.setImageDrawable(drawable);
            styleBtn.setImageDrawable(drawableStyle);
            linearLayout.addView(colorBtn,0);
            linearLayout.addView(styleBtn,1);
            colorBtn.setOnClickListener(v1 -> {
                editInput.clearFocus();
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, editInput.getCurrentTextColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        editInput.setTextColor(color);
                        colorBtn.getDrawable().setTint((color));
                        atomicInteger.set(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });
                dialog.show();
            });
            styleBtn.setOnClickListener(v1 -> {
                View customView = LayoutInflater.from(this).inflate(R.layout.dialog_danmaku_style, null);
                AutoCompleteTextView dropdown = customView.findViewById(R.id.dropdown_menu_danmaku_mode);
                Slider sliderDS = customView.findViewById(R.id.slider_danmaku_size);
                EditText textEditDS = customView.findViewById(R.id.input_value_ds);
                List<String> items = List.of(getString(R.string.danmaku_float), getString(R.string.danmaku_Top), getString(R.string.danmaku_bottom));
                List<String> itemValues = List.of("scroll","top", "bottom");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
                dropdown.setAdapter(adapter);
                dropdown.setText(items.get(itemValues.indexOf(atomicDanmakuType.get())),false);

                sliderDS.setValue(atomicTextSize.get());
                textEditDS.setText(String.valueOf(atomicTextSize.get()));
                sliderDS.addOnChangeListener((slider, v2, fromUser) -> {
                    if (fromUser) {

                        textEditDS.setText(String.valueOf((int) v2));
                    }
                });
                textEditDS.addTextChangedListener(new TextWatcher(){
                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            int val = Integer.parseInt(s.toString());
                            val= (int) Math.clamp(val,sliderDS.getValueFrom(),sliderDS.getValueTo());
                            sliderDS.setValue(val);
                        } catch (NumberFormatException e) {
                            // 忽略空值或非法输入
                        }
                    }
                });

                new MaterialAlertDialogBuilder(this)
                        .setView(customView)
                        .setPositiveButton("确定", (dialog, which) -> {
                            atomicDanmakuType.set(itemValues.get(items.indexOf(dropdown.getText().toString())));
                            atomicTextSize.set((int) sliderDS.getValue());
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
            rootView.addView(linearLayout,0);
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            bottomSheetDialog.show();
        });

        if (type.equals(TYPE_AUDIT)) {
            runOnUiThread(()->{
                    setDanmakuEnable(false);
                    findViewById(R.id.horizontalScrollView).setVisibility(GONE);
                    findViewById(R.id.horizontalScrollView).setVisibility(GONE);
                    dynamicFragmentAdapter.addFragment(AuditVideoFragment.newInstance(netData), getString(R.string.audit));

            });
        }
        runOnUiThread(()->{
            Point size = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
                Insets insets = metrics.getWindowInsets()
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
                int width = metrics.getBounds().width() - insets.left - insets.right;
                int height = metrics.getBounds().height() - insets.top - insets.bottom;
                size.set(width, height);
            } else {
                Display display = getWindowManager().getDefaultDisplay();
                display.getSize(size);  // 这是 Point(width, height)
            }

            int orientation = getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewPager.setVisibility(GONE);
                findViewById(R.id.horizontalScrollView).setVisibility(GONE);
                View fullscreenView = findViewById(R.id.video_view_wrapper);
                ViewGroup.LayoutParams params = fullscreenView.getLayoutParams();
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                int visibleWidth = rect.width();
                int visibleHeight = rect.height();
                params.width = visibleWidth;
                params.height = visibleHeight;
                fullscreenView.setLayoutParams(params);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }else {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
            }
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }else {
                        this.setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
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
    // 调用系统方法创建 PopupWindow
    @SuppressLint("SetTextI18n")
    private void showDanmakuFloatingPopup(View parent, float x, float y, DanmakuItem danmakuItem) {
        // 使用 LayoutInflater 从 XML 构建视图
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View popupView = inflater.inflate(R.layout.layout_popup_danmaku, getRoot(),false); // 替换成你的 XML
        PopupWindow popup = new PopupWindow(popupView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true);
        ((TextView) popupView.findViewById(R.id.tvContent)).setText(getText(R.string.danmaku)+": "+danmakuItem.getData().getContent());
        popupView.findViewById(R.id.btn_report).setOnClickListener(v -> {
            popup.dismiss();
            AlertUtil.showYesNo(v.getContext(), getString(R.string.report), getString(R.string.report_danmaku, danmakuItem.getData().getContent()), (dialog, which) -> {
                Thread thread = new Thread(() -> {
                    EmptyResult emptyResult = ApiUtil.getAppApi().getDanmakuApi().report_danmaku(danmakuItem.getData().getDanmakuId());
                    ApiUtil.throwApiError(emptyResult);
                    runOnUiThread(() -> {
                        Toast.makeText(VideoPlayerActivity.this, R.string.ok, Toast.LENGTH_SHORT).show();
                    });
                });
                thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(VideoPlayerActivity.this));
                thread.start();
            }, (dialog, which) -> {

            }).show();
        });
        popupView.findViewById(R.id.btn_copy).setOnClickListener(v -> {
            popup.dismiss();
            UiUtil.copyToClipboard(v.getContext(), danmakuItem.getData().getContent());
            Toast.makeText(this, R.string.copy, Toast.LENGTH_SHORT).show();
        });



        // 设置点击外部消失
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popup.setOnDismissListener(() -> danmakuPlayer.hold(null));
        // 在点击位置显示
        popup.showAtLocation(parent, Gravity.NO_GRAVITY,
                (int) x, (int) y);
    }
    @SuppressLint("ClickableViewAccessibility")
    @OptIn(markerClass = UnstableApi.class)
    private void initPlayer() {
        // 创建媒体播放器
        View fullscreenView = findViewById(R.id.video_view_wrapper);
        danmakuView=findViewById(R.id.sv_danmaku);
        mediaPlayer = new ExoPlayer.Builder(this).build();
        runOnUiThread(()->{
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            videoSurface.setPlayer(mediaPlayer);
            videoSurface.setFullscreenButtonState(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE);
            videoSurface.setFullscreenButtonClickListener(isFullscreen -> {
                if (isFullscreen){
                    hideNavigationBar();
                }
                setRequestedOrientation(isFullscreen?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            });

            videoSurface.setResizeMode(getScaleTypeVideoInt(this));
            getRoot().post(()->{
                if (getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE) {
                    fullscreenView.setLayoutParams(new LinearLayout.LayoutParams(fullscreenView.getWidth(), (int) (UiUtil.getAppWindowHeight(VideoPlayerActivity.this) * 0.354)));
                    viewPager.setLayoutParams(new LinearLayout.LayoutParams(viewPager.getWidth(), UiUtil.getAppWindowHeight(VideoPlayerActivity.this) - findViewById(R.id.horizontalScrollView).getHeight()));
                }
            });
            danmakuView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    var danmakus = danmakuPlayer.getDanmakusAtPoint(new Point((int) event.getX(), (int) event.getY()));
                    if (danmakus==null||danmakus.isEmpty()){
                        return false;
                    }
                    var danmaku = danmakus.get(0);
                    danmakuPlayer.hold(null);
                    danmakuPlayer.hold(danmaku);
                    showDanmakuFloatingPopup(danmakuView,event.getRawX(),event.getRawY(),danmaku);
                    // 响应点击逻辑
                    Log.d("DanmakuView", "弹幕被点击了");
                }
                // 返回 false，让事件继续传递到下一层
                return false;
            });
        });

        mediaPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying){
                    if (isFinishing()) return;
                    danmakuPlayer.start(danmakuConfig);
                    danmakuPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    //updateVideoScaling(videoSurface.getWidth(),videoSurface.getHeight());
                }else {
                    if (danmakuPlayer!=null) {
                        danmakuPlayer.pause();
                    }
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Player.Listener.super.onPositionDiscontinuity(reason);
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
                    case Player.STATE_READY: {
                        if (!UiUtil.getScaleTypeVideo(VideoPlayerActivity.this).equals("auto")) break;
                        VideoSize videoSize = mediaPlayer.getVideoSize();
                        var vVideo = videoSize.height>videoSize.width;
                        if(vVideo&&getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE){
                            var sizeW = fullscreenView.getWidth();
                            var sizeH = fullscreenView.getHeight();
                            var nSizeH = ((float)sizeW)*videoSize.height/videoSize.width;
                            nSizeH = Math.clamp(nSizeH,sizeH, UiUtil.getAppWindowHeight(VideoPlayerActivity.this)*0.60f);
                            fullscreenView.setLayoutParams(new LinearLayout.LayoutParams(sizeW,(int)nSizeH));
                            var pcH = (nSizeH/(float) sizeH);
                            if (pcH>0.8f&&pcH<0.98f){
                                videoSurface.setResizeMode(RESIZE_MODE_FILL);
                            }
                        }else {
                            if (videoSize.width>videoSize.height)
                                videoSurface.setResizeMode(RESIZE_MODE_FILL);
                            else
                                videoSurface.setResizeMode(RESIZE_MODE_FIT);
                        }
                        break;
                    }
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
    /**
     * 反射获取 PlayerView 内部的 StyledPlayerControlView (controller)。
     */
    public static PlayerControlView getController(PlayerView playerView) {
        try {
            Field field = PlayerView.class.getDeclaredField("controller");
            field.setAccessible(true);
            Object obj = field.get(playerView);
            if (obj instanceof PlayerControlView) {
                return (PlayerControlView) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                    if (type.equals(TYPE_VIEW)) {
                        netData = MyApp.getInstance().getOttohubApi().getVideoApi().get_video_detail(vid);
                    } else if (type.equals(TYPE_AUDIT)) {
                        netData = new Gson().fromJson(data, VideoResult.class);
                        netData.status= ApiResultBase.SUCCESS;
                    }
                }else {
                    netData = gson.fromJson(savedInstanceState.getString(KEY_NET_DATA),VideoResult.class);
                    Log.d(TAG, "loadData: net data from storage");
                }
                if (!savedInstanceState.containsKey(KEY_DANMAKU_DATA)) {
                    danmakuData = MyApp.getInstance().getOttohubApi().getDanmakuApi().get_danmaku(vid);
                    if (type.equals(TYPE_AUDIT)) {
                        danmakuData.status= ApiResultBase.SUCCESS;
                    }
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
            if (mediaPlayer == null) return;

            if (type.equals(TYPE_VIEW)) {
                var vdf = VideoDescribeFragment.newInstance(netData);
                vdf.videoPlayerActivity=this;
                dynamicFragmentAdapter.addFragment(vdf, getString(R.string.describe));
                dynamicFragmentAdapter.addFragment(FragmentFragment.newInstance(CommentFragmentBase.newInstance(netData.vid,0,CommentFragmentBase.TYPE_VIDEO)), getString(R.string.comment));
            }
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
                    data.danmaku_id,
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
            Log.d(TAG, "loadDanmaku: 添加弹幕:"+data.text);
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
        if (mediaPlayer==null) return;
        mediaPlayer.setMediaSource(mediaSource);
    }
    private void initDanmaku() {
        danmakuPlayer = new DanmakuPlayer(new UnlockedDanmakuRender(),new DataSource());
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