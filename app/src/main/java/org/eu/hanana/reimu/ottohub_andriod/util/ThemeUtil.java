package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.activity.BaseActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.ui.ThemeData;

import java.util.List;

public class ThemeUtil {
    private static boolean dirty = true;
    private static ThemeData themeData;
    public static void saveTheme(Context context, ThemeData newColor){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("user_color", new Gson().toJson(newColor)).commit();
        dirty=true;
    }
    public static ThemeData getTheme(Context c){
        if (!dirty&&themeData!=null){
            return themeData;
        }
        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (!prefs.contains("user_color")){
            saveTheme(c,ThemeData.DEFAULT);
        }
        dirty=false;
        themeData=gson.fromJson(prefs.getString("user_color", "ERROR"), ThemeData.class);
        return themeData;
    }
    public static void onCreate(AppCompatActivity activity) {
        var t = getTheme(activity);
    }
    public static void onPostCreate(AppCompatActivity activity) {
        var userColor = getTheme(activity);
        var views = UiUtil.getAllViews(activity);
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar!=null) {
            supportActionBar.setBackgroundDrawable(new ColorDrawable(userColor.getColorActionBar()));
        }
        if (activity instanceof BaseActivity){
            var ba=(BaseActivity) activity;
            views.add(ba.toolbar);
        }
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), true);
        // 设置系统栏颜色
        activity.getWindow().setStatusBarColor(userColor.getColorActionBar());
        activity.getWindow().setNavigationBarColor(userColor.getColorActionBar());
        apply(views,userColor);
    }
    public static void onViewCreated(Fragment fragment) {
        var userColor = getTheme(fragment.getContext());
        var views = UiUtil.getAllViews(fragment);
        apply(views,userColor);
    }
    public static void apply(View view) {
        var userColor = getTheme(view.getContext());
        List<View> views = null;
        if (view instanceof ViewGroup) {
            views = UiUtil.getAllViews(((ViewGroup) view));
        }else {
            views = UiUtil.getAllViews(view);
        }
        apply(views, userColor);
    }
    public static void apply(List<View> views, ThemeData userColor){
        for (View view : views) {
            if (!String.valueOf(view.getTag()).contains("themed")) continue;
            if (view instanceof Button){
                var button = (Button) view;
                if (!String.valueOf(view.getTag()).contains("icon")) {
                    button.setBackgroundTintList(createProgressBarColorStateList(userColor.getColorPrimary()));
                    button.setTextColor(createProgressBarColorStateList(userColor.getColorOnPrimary()));
                }
            }
            if (view instanceof ProgressBar){
                var progressBar = (ProgressBar) view;
                var tint = createProgressBarColorStateList(userColor.getColorPrimary());
                progressBar.setIndeterminateTintList(tint);
                progressBar.setProgressTintList(tint);
                progressBar.setSecondaryProgressTintList(tint);
                progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            }
            if (view instanceof BottomNavigationView){
                var item = ((BottomNavigationView) view);
                item.setBackgroundColor(userColor.getColorActionBar());
                item.setItemIconTintList(createCheckingColorStateList(userColor.getColorPrimaryVariant()));
                item.setItemTextColor(ColorStateList.valueOf(userColor.getColorOnPrimary()));
            }
            if (view instanceof Toolbar){
                var item = ((Toolbar) view);
                item.getOverflowIcon().setTint(userColor.getColorOnPrimary());
                item.setTitleTextColor(userColor.getColorOnPrimary());
                if (item.getNavigationIcon() != null) {
                    item.getNavigationIcon().setTint(userColor.getColorOnPrimary());
                }
                item.post(() -> {
                    for (int i = 0; i < item.getMenu().size(); i++) {
                        MenuItem itemM = item.getMenu().getItem(i);
                        if (itemM.getIcon() != null) {
                            itemM.getIcon().setTint(userColor.getColorOnPrimary());
                        }
                    }
                });
            }
            if (view instanceof TextView){
                var item = ((TextView) view);
                if (String.valueOf(item.getTag()).contains("second")){
                    item.setTextColor(userColor.getColorOnPrimarySecond());
                }else {
                    item.setTextColor(userColor.getColorOnPrimary());
                }
            }
            if (view instanceof TabLayout){
                var item = ((TabLayout) view);
                item.setBackgroundColor(userColor.getColorBackground());
                item.setSelectedTabIndicatorColor(userColor.getColorPrimary());
                item.setTabTextColors(Color.GRAY,userColor.getColorPrimary());
                item.setTabRippleColor(createFocusStateList(userColor.getColorPrimary()));
            }
            if (view instanceof ImageView){
                var item = ((ImageView) view);
                item.setImageTintList(ColorStateList.valueOf(userColor.getColorOnPrimary()));
            }
            if (view instanceof EditText){
                var item = ((EditText) view);
                item.setTextColor(userColor.getColorOnPrimary());
                item.setHintTextColor(userColor.getColorOnPrimarySecond());
            }
            if (view instanceof EditText){
                var item = ((EditText) view);
                item.setTextColor(userColor.getColorOnPrimary());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Drawable textCursorDrawable = item.getTextCursorDrawable();
                    if (textCursorDrawable != null) {
                        textCursorDrawable.setTint(userColor.getColorPrimary());
                    }
                }

            }
            if (view instanceof MaterialButton){
                var item = ((MaterialButton) view);
                if (!String.valueOf(view.getTag()).contains("icon")) {
                    item.setIconTint(createIconTintList(userColor.getColorOnPrimary()));
                }else {
                    item.setIconTint(ColorStateList.valueOf(userColor.getColorPrimary()));
                    item.setTextColor(userColor.getColorPrimary());
                }
            }
            if (view instanceof TextInputLayout){
                var item = ((TextInputLayout) view);
                item.setHintTextColor(new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_focused}, // 获得焦点
                                new int[]{}                              // 默认
                        },
                        new int[]{
                                userColor.getColorPrimary(), // 聚焦时主题色
                                darkenColor(userColor.getColorOnPrimary()) // 默认较浅
                        }
                ));
                item.setBoxStrokeColorStateList(new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_focused}, // 获得焦点
                                new int[]{}                              // 默认
                        },
                        new int[]{
                                userColor.getColorPrimary(), // 聚焦时主题色
                                darkenColor(userColor.getColorOnPrimary()) // 默认较浅
                        }
                ));
            }
            if (String.valueOf(view.getTag()).contains("background")){
                if (view instanceof CardView){
                    ((CardView) view).setCardBackgroundColor(userColor.getColorBackground());
                }else if(view.getBackground()==null){
                    view.setBackgroundColor(userColor.getColorBackground());
                }else {
                    view.getBackground().setTint(userColor.getColorBackground());
                }
            }
        }
    }
    @ColorInt
    public static int invertColor(@ColorInt int color) {
        int a = Color.alpha(color);
        int r = 255 - Color.red(color);
        int g = 255 - Color.green(color);
        int b = 255 - Color.blue(color);
        return Color.argb(a, r, g, b);
    }
    public static ColorStateList createIconTintList(int baseColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed}, // 按下
                        new int[]{android.R.attr.state_focused}, // 获得焦点
                        new int[]{}                              // 默认
                },
                new int[]{
                        ColorUtils.setAlphaComponent(baseColor, 0xFF),  // 按下：完全不透明
                        ColorUtils.setAlphaComponent(baseColor, 0xAA),  // 焦点：稍微浅一点
                        ColorUtils.setAlphaComponent(baseColor, 0x88)   // 默认：更淡
                }
        );
    }
    public static ColorStateList createFocusStateList(int rippleColor){
        // 按下
        // 获得焦点
        // 默认
        // 按下半透明
        // 焦点更浅
        // 默认透明
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed}, // 按下
                        new int[]{android.R.attr.state_focused}, // 获得焦点
                        new int[]{}                              // 默认
                },
                new int[]{
                        ColorUtils.setAlphaComponent(rippleColor, 80),  // 按下半透明
                        ColorUtils.setAlphaComponent(rippleColor, 60),  // 焦点更浅
                        ColorUtils.setAlphaComponent(rippleColor, 0)    // 默认透明
                }
        );
    }
    public static ColorStateList createSelectColorStateList(int colorNormal, int colorSelected) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_selected }, // 选中状态
                new int[] { -android.R.attr.state_selected } // 未选中状态
        };

        int[] colors = new int[] {
                colorSelected,
                colorNormal
        };

        return new ColorStateList(states, colors);
    }
    public static ColorStateList createCheckingColorStateList(@ColorInt int baseColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},   // 选中
                new int[]{-android.R.attr.state_checked}   // 未选中
        };

        int[] colors = new int[]{
                baseColor,
                darkenColor(baseColor,0.6f)
        };

       return new ColorStateList(states, colors);
    }
    public static ColorStateList createProgressBarColorStateList(@ColorInt int baseColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},   // 按下
                new int[]{-android.R.attr.state_enabled},  // 禁用
                new int[]{}                                 // 默认
        };

        int[] colors = new int[]{
                darkenColor(baseColor,0.9f),
                darkenColor(baseColor,0.6f),
                baseColor
        };

        return new ColorStateList(states, colors);
    }
    // 简单调暗函数，用于状态栏颜色
    private static int darkenColor(int color,float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }
    private static int lightenColor(int color, float factor) {
        int r = (int) (Color.red(color) + (255 - Color.red(color)) * factor);
        int g = (int) (Color.green(color) + (255 - Color.green(color)) * factor);
        int b = (int) (Color.blue(color) + (255 - Color.blue(color)) * factor);
        return Color.rgb(r, g, b);
    }
    private static int darkenColor(int color) {
       return darkenColor(color,0.85f);
    }
}
