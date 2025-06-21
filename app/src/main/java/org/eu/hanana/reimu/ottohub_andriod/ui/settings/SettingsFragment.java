package org.eu.hanana.reimu.ottohub_andriod.ui.settings;

import static android.content.Intent.getIntent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.AboutActivity;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CacheUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.LocaleHelper;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // 绑定“清除缓存”点击事件
        Preference clearCachePref = findPreference("clear_cache");
        if (clearCachePref != null) {
            clearCachePref.setOnPreferenceClickListener(preference -> {
                var alertDialog = AlertUtil.showLoading(getContext(), getString(R.string.loading));
                CacheUtil.clearCacheAsync(getContext(), success -> {
                    if (success) {
                        Toast.makeText(getContext(), "缓存已清除", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "清除缓存失败", Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                });
                alertDialog.show();
                return true;
            });
        }
        Preference logout = findPreference("logout");
        if (logout != null) {
            logout.setOnPreferenceClickListener(preference -> {
                ApiUtil.logout();
                Intent intent = getActivity().getIntent();
                getActivity().finish();             // 结束当前 Activity
                startActivity(intent); // 用原始 Intent 重启
                return true;
            });
        }

        Preference about = findPreference("about");
        if (about != null) {
            about.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), AboutActivity.class));
                return true;
            });
        }

        SwitchPreference autoLoginPref = findPreference("auto_login");
        if (autoLoginPref != null) {
            // 读取当前状态
            boolean isAutoLogin = autoLoginPref.isChecked();

            // 监听开关变化
            autoLoginPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                // TODO: 根据 enabled 处理自动登录逻辑
                // 例如保存状态或调用相关方法

                return true; // 返回 true 表示保存此值
            });
        }

    }
}
