package org.eu.hanana.reimu.ottohub_andriod.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

public class ThemeUtil {

    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_PRIMARY_COLOR = "primary_color";

    private static final String DEFAULT_COLOR = "#6200EE"; // 默认紫色

    /**
     * 保存主颜色（Hex 格式，如 "#FF5722"）
     */
    public static void savePrimaryColor(@NonNull Context context, @NonNull String colorHex) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PRIMARY_COLOR, colorHex).apply();
    }

    /**
     * 获取当前主颜色的字符串（如 "#6200EE"）
     */
    @NonNull
    public static String getPrimaryColorHex(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PRIMARY_COLOR, DEFAULT_COLOR);
    }

    /**
     * 获取当前主颜色的 int 值
     */
    @ColorInt
    public static int getPrimaryColorInt(@NonNull Context context) {
        return Color.parseColor(getPrimaryColorHex(context));
    }

    /**
     * 应用主色到 Toolbar 和状态栏（可选）
     */
    public static void applyPrimaryColor(Activity activity) {
        int color = getPrimaryColorInt(activity);

        applyPrimaryColorToActionBar(activity);


        // 设置状态栏颜色
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(darkenColor(color, 0.85f)); // 可选：暗一点
        }
    }
    /**
     * 应用主色到原生 ActionBar
     */
    public static void applyPrimaryColorToActionBar(Activity activity) {
        int color = getPrimaryColorInt(activity);
        ActionBar actionBar = activity.getActionBar(); // 原生 ActionBar
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }
    /**
     * 应用颜色到任意 View（如根布局）
     */
    public static void applyPrimaryColorToView(View view, Context context) {
        int color = getPrimaryColorInt(context);
        view.setBackgroundColor(color);
    }

    /**
     * 应用颜色到 TextView 字体
     */
    public static void applyPrimaryTextColor(TextView textView, Context context) {
        int color = getPrimaryColorInt(context);
        textView.setTextColor(color);
    }

    /**
     * 切换颜色并重启当前 Activity 生效
     */
    public static void switchPrimaryColor(Activity activity, String colorHex) {
        savePrimaryColor(activity, colorHex);
        restartActivity(activity);
    }

    /**
     * 重启当前 Activity
     */
    public static void restartActivity(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }

    /**
     * 颜色变暗（用于状态栏等）
     */
    private static int darkenColor(int color, float factor) {
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }
}
