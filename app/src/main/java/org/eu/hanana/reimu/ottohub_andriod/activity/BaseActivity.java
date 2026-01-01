package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import org.eu.hanana.reimu.ottohub_andriod.MainActivity;
import org.eu.hanana.reimu.ottohub_andriod.util.LocaleUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

public abstract class BaseActivity extends AppCompatActivity {
    public  MaterialToolbar toolbar;
    public LinearLayout wrapper;
    public ViewGroup getRoot(){
        return (ViewGroup) findViewById(android.R.id.content);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtil.updateLocale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar=new MaterialToolbar(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        var rootView = getRoot();
        // 获取现有布局
        View existingLayout = rootView.getChildAt(0); // 原来的布局
        rootView.removeView(existingLayout); // 移除原布局

        // 创建新的 LinearLayout
        wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        // 添加原有布局到 wrapper
        wrapper.addView(existingLayout, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        // 再把 wrapper 添加回顶层
        rootView.addView(wrapper);
        // 动态创建 Toolbar
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;

        // 从当前主题获取 actionBarSize
        if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, getResources().getDisplayMetrics());
        }
        toolbar.setLayoutParams(new MaterialToolbar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                actionBarHeight // ActionBar 标准高度
        ));
        toolbar.setTag("themed");
        wrapper.addView(toolbar,0);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 增加状态栏高度
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).topMargin = systemBars.top;
            }
            v.setLayoutParams(lp);
            return insets;
        });
        if (this.getClass()!= MainActivity.class&&needBottomPadding()) {
            ViewCompat.setOnApplyWindowInsetsListener(wrapper, (v, insets) -> {
                var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });
        }
        ThemeUtil.onPostCreate(this);
    }

    protected boolean needBottomPadding(){
        return true;
    }
}
