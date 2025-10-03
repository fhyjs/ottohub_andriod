package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.ui.ThemeData;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

public class ThemeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_theme);
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.theme_settings);
        LinearLayout tl = findViewById(R.id.ll_theme_list);
        var tList = new ArrayList<>(List.of(
                ThemeData.DEFAULT,
                ThemeData.DARK,
                ThemeData.SANAE,
                ThemeData.BILI,
                ThemeData.PURPLE_MD2,
                ThemeData.CHINA_RED
                ));
        for (ThemeData themeData : tList) {

            View inflate = getLayoutInflater().inflate(R.layout.item_theme_card,tl,false);
            TextView textName = inflate.findViewById(R.id.tv_name);
            textName.setText(UiUtil.getStringByName(this, "theme_" + themeData.getName()));
            textName.setTextColor(themeData.getColorOnPrimary());
            inflate.findViewById(R.id.mcv_background).setBackgroundColor(themeData.getColorBackground());
            tl.addView(inflate);
            inflate.setOnClickListener(v -> {
                ThemeUtil.saveTheme(this,themeData);
                Intent intent = getPackageManager()
                        .getLaunchIntentForPackage(getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Runtime.getRuntime().exit(0); // 确保进程结束
                }
            });
        }
        findViewById(R.id.btn_include).setOnClickListener(v -> {

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
}