package org.eu.hanana.reimu.ottohub_andriod.ui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

import com.google.android.material.materialswitch.MaterialSwitch;

import org.eu.hanana.reimu.ottohub_andriod.data.ui.ThemeData;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

public class M3SwitchPreference extends SwitchPreference {

    public M3SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public M3SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public M3SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public M3SwitchPreference(@NonNull Context context) {
        super(context);
    }

    @SuppressLint({"InlinedApi", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ThemeData theme = ThemeUtil.getTheme(getContext());
        ((TextView) holder.findViewById(android.R.id.title)).setTextColor(theme.getColorOnPrimary());
        ((TextView) holder.findViewById(android.R.id.summary)).setTextColor(theme.getColorOnPrimarySecond());
        getIcon().setTint(theme.getColorOnPrimary());

        var ms = ((MaterialSwitch) holder.findViewById(android.R.id.switch_widget));
        // 滑块颜色
        ColorStateList thumbColors = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        Color.WHITE, // checked
                        Color.DKGRAY   // unchecked
                });
        ms.setThumbTintList(thumbColors);

// 轨道颜色
        ColorStateList trackColors = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        theme.getColorPrimary(),
                        Color.GRAY
                });
        ms.setTrackTintList(trackColors);
        ms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setChecked(isChecked);
        });
    }
}
