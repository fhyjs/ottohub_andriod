package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;

import androidx.annotation.Px;

public class UiUtil {
    @Px
    public static int dpToPx(Context context,int dp){
        // 你想设置的 dp 值
        float scale = context.getResources().getDisplayMetrics().density;
        // 四舍五入
        return (int) (dp * scale + 0.5f);
    }
}
