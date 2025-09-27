package org.eu.hanana.reimu.ottohub_andriod.ui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.eu.hanana.reimu.ottohub_andriod.data.ui.ThemeData;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

public class MaterialListPreference extends ListPreference {

    public MaterialListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialListPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        // 可自定义 widget 布局
        //setWidgetLayoutResource(R.layout.preference_widget_material_icon);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ThemeData theme = ThemeUtil.getTheme(getContext());
        ((TextView) holder.findViewById(android.R.id.title)).setTextColor(theme.getColorOnPrimary());
        ((TextView) holder.findViewById(android.R.id.summary)).setTextColor(theme.getColorOnPrimarySecond());
        getIcon().setTint(theme.getColorOnPrimary());
    }

    @Override
    protected void onClick() {
        // 点击时显示 MaterialAlertDialog
        Context context = getContext();
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int checkedItem = findIndexOfValue(getValue());

        new MaterialAlertDialogBuilder(context)
                .setTitle(getDialogTitle())
                .setSingleChoiceItems(entries, checkedItem, (dialog, which) -> {
                    String value = entryValues[which].toString();
                    if (callChangeListener(value)) {
                        setValue(value);
                    }
                    dialog.dismiss();
                })
                .show();
    }
}
