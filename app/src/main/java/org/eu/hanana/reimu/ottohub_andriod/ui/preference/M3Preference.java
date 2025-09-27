package org.eu.hanana.reimu.ottohub_andriod.ui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.materialswitch.MaterialSwitch;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.ui.ThemeData;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

public class M3Preference extends Preference {
    public M3Preference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public M3Preference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public M3Preference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public M3Preference(@NonNull Context context) {
        super(context);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ThemeData theme = ThemeUtil.getTheme(getContext());
        ((TextView) holder.findViewById(android.R.id.title)).setTextColor(theme.getColorOnPrimary());
        ((TextView) holder.findViewById(android.R.id.summary)).setTextColor(theme.getColorOnPrimarySecond());
        getIcon().setTint(theme.getColorOnPrimary());

    }
}
