package org.eu.hanana.reimu.ottohub_andriod.ui.settings;

import static android.content.Intent.getIntent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.AboutActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ThemeActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.IScrollTopChecker;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CacheUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.LocaleHelper;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UpdateUtil;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements IScrollTopChecker {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        //this.getView().setBackgroundColor(0xFFfdfdfd);
        ThemeUtil.onViewCreated(this);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setTag("themed|background");
        return view;
    }

    @Override
    public boolean atTop() {
        return !getListView().canScrollVertically(-1);
    }

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
        Preference reset = findPreference("reset");
        if (reset != null) {
            reset.setOnPreferenceClickListener(preference -> {
                AlertUtil.showYesNo(getContext(),getString(R.string.reset),getString(R.string.issure),(dialog, which) -> {
                    // 1. 清除现有设置
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .edit().clear().apply();

                    // 2. 重新加载 XML 默认值
                    PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, true);

                    // 3. 刷新当前界面（让 UI 更新成默认值）
                    getActivity().recreate();
                },null).show();
                return true;
            });
        }
        Preference language = findPreference("language");
        if (language != null) {
            language.setOnPreferenceChangeListener((preference, newValue) -> {
                getActivity().recreate();
                return true;
            });
        }
        Preference update_now = findPreference("update_now");
        if (update_now != null) {
            update_now.setOnPreferenceClickListener(preference -> {
                UpdateUtil.getUpdaterChecker(this.getActivity()).start();
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
        Preference theme = findPreference("theme");
        if (theme != null) {
            theme.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), ThemeActivity.class));
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
