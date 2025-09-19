package org.eu.hanana.reimu.ottohub_andriod.activity;

import static android.view.View.GONE;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoListResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoPagerAdapter;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortVideoActivity extends BaseActivity {
    private static final String TAG = "ShortVideoActivity";
    private ViewPager2 viewPager;
    private List<VideoResult> videoUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_short_video);
        toolbar.setVisibility(GONE);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new VideoPagerAdapter(this, videoUrls));
        viewPager.setOffscreenPageLimit(2);

        // 页面切换自动播放
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int currentPosition = 0;

            @Override
            public void onPageSelected(int position) {
                Fragment prev = getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
                if (prev instanceof VideoFragment) ((VideoFragment) prev).releasePlayer();

                Fragment current = getSupportFragmentManager().findFragmentByTag("f" + position);
                if (current instanceof VideoFragment) ((VideoFragment) current).initPlayer();

                currentPosition = position;
                int lastIndex = videoUrls.size() - 1;
                if (position == lastIndex) {
                    // 到达底部，加载更多视频
                    loadMoreVideos();
                }
            }
        });
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        loadMoreVideos();
    }
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    public void finish() {
        super.finish();
        Fragment current = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (current instanceof VideoFragment) ((VideoFragment) current).releasePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    private void loadMoreVideos() {
        Log.i(TAG, "loadMoreVideos: load");
        Thread thread = new Thread(() -> {
            // 获取视频列表
            VideoListResult videoListResult = ApiUtil.getAppApi().getVideoApi().random_video_list(6);
            ApiUtil.throwApiError(videoListResult);

            for (VideoResult videoResult : videoListResult.video_list) {
                try {
                    // 请求单个视频详情
                    VideoResult detail = ApiUtil.getAppApi().getVideoApi().get_video_detail(videoResult.vid);
                    ApiUtil.throwApiError(detail);
                    // 在主线程更新数据和 Adapter
                    runOnUiThread(() -> {
                        int oldSize = videoUrls.size();
                        videoUrls.add(detail);
                        viewPager.getAdapter().notifyItemInserted(oldSize);
                    });

                } catch (Exception ignored) {
                    // 单个视频请求失败就跳过
                }
            }
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
        thread.start();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(wrapper, (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}