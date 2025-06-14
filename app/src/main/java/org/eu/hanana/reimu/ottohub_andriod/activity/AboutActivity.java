package org.eu.hanana.reimu.ottohub_andriod.activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.button.MaterialButton;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.BuildConfig;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.AppVersionUtil;

import java.util.Random;

public class AboutActivity extends AppCompatActivity {
    protected int clickTimes;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ((TextView) findViewById(R.id.tvVersion)).setText(AppVersionUtil.getVersionName(this)+"("+AppVersionUtil.getVersionCode(this)+")");
        ((TextView) findViewById(R.id.tvPackage)).setText(AppVersionUtil.getPackageName(this));
        ((TextView) findViewById(R.id.tvTime)).setText(BuildConfig.BUILD_TIME);
        findViewById(R.id.imageView).setOnClickListener(v -> {
            clickTimes++;
            if (clickTimes>=4) {
                makeLayoutMessy(findViewById(R.id.container));
                ((ImageView) findViewById(R.id.imageView)).setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.hanana));
                var button = new MaterialButton(this);
                button.setIcon(AppCompatResources.getDrawable(this,R.drawable.ottoicon));
                button.setText(getText(R.string.ottohub));
                button.setIconTint(null); // 彻底取消 tint
                // 生成随机颜色
                var random = new Random();
                int randomColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));

                // 使用 ColorStateList 创建按钮背景色（含 Ripple）
                ColorStateList bgColor = ColorStateList.valueOf(randomColor);
                button.setBackgroundTintList(bgColor);
                button.setOnClickListener(v1 -> {
                    new Thread(()->{
                        VideoResult videoResult = ApiUtil.getAppApi().getVideoApi().random_video_list(1).video_list.get(0);
                        runOnUiThread(()->{
                            Intent intent = new Intent(this, VideoPlayerActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt(VideoPlayerActivity.KEY_VID,videoResult.vid);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        });
                    }).start();
                });
                ((LinearLayout) findViewById(R.id.container)).addView(button);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 默认返回栈顶页面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void makeLayoutMessy(ViewGroup rootLayout) {
        Random random = new Random();

        int width = rootLayout.getWidth();
        int height = rootLayout.getHeight();

        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            View child = rootLayout.getChildAt(i);

            // 随机位置偏移（-100 到 +100 px 范围内）
            float offsetX = (random.nextFloat() - 0.5f) * 200;
            float offsetY = (random.nextFloat() - 0.5f) * 200;

            // 随机旋转角度 (-30 ~ +30度)
            float rotation = (random.nextFloat() - 0.5f) * 60;

            // 随机缩放 (0.7 ~ 1.3)
            float scale = 0.7f + random.nextFloat() * 0.9f;

            // 设置属性
            child.animate()
                    .translationXBy(offsetX)
                    .translationYBy(offsetY)
                    .rotation(rotation)
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(500)
                    .start();
        }
    }

}