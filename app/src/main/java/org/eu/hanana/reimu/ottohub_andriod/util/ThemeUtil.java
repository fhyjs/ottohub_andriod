package org.eu.hanana.reimu.ottohub_andriod.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DeprecatedSinceApi;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.eu.hanana.reimu.ottohub_andriod.R;

import java.lang.reflect.Field;

@Deprecated
public class ThemeUtil {
    public static Resources.Theme getVTheme(AppCompatActivity activity){
        Resources.Theme theme = activity.getResources().newTheme();
        theme.applyStyle(R.style.Theme_Ottohub_andriod, true);
        TypedValue typedValueColor = new TypedValue();
        typedValueColor.type=TypedValue.TYPE_INT_COLOR_ARGB8;
        typedValueColor.data=0xffff0000;
        setValue(activity,typedValueColor, androidx.appcompat.R.attr.colorPrimary);
        return theme;
    }
    public static void setValue(Context context, TypedValue typedValue,int id) {
        Resources.Theme theme = context.getTheme();

        // applyStyle 只能应用资源 id，所以需要间接方式：
        // 使用 reflection 动态替换 mThemeImpl 中的 colorPrimary
        try {
            java.lang.reflect.Field themeImplField = theme.getClass().getDeclaredField("mThemeImpl");
            themeImplField.setAccessible(true);
            Object themeImpl = themeImplField.get(theme);

            java.lang.reflect.Method applyAttrMethod =
                    themeImpl.getClass().getDeclaredMethod(
                            "applyStyleAttribute", int.class, TypedValue.class, boolean.class);
            applyAttrMethod.setAccessible(true);

            applyAttrMethod.invoke(themeImpl, id, typedValue, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void applyVTheme(AppCompatActivity activity){
        Resources.Theme vTheme = getVTheme(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.setTheme(vTheme);
        }else{
            applyThemeCompat(activity, vTheme);
        }

    }
    @DeprecatedSinceApi(api = 36)
    public static void applyThemeCompat(Context context, Resources.Theme newTheme) {
        try {
            // 找到 ContextThemeWrapper 类（Activity 继承自它）
            Class<?> clazz = Class.forName("android.view.ContextThemeWrapper");

            // 取到 mTheme 字段
            @SuppressLint("SoonBlockedPrivateApi") Field themeField = clazz.getDeclaredField("mTheme");
            themeField.setAccessible(true);

            // 赋值新的 Theme
            themeField.set(context, newTheme);

            // 同时把 mThemeResource 置 0，避免被覆盖
            Field resField = clazz.getDeclaredField("mThemeResource");
            resField.setAccessible(true);
            resField.setInt(context, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
