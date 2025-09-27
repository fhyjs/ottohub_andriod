package org.eu.hanana.reimu.ottohub_andriod.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class DynamicFragmentAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>(); // 如果需要配合 TabLayout

    public DynamicFragmentAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    // 添加 Fragment
    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
        notifyItemInserted(fragments.size() - 1);
    }

    // 删除 Fragment
    public void removeFragment(int position) {
        if (position < 0 || position >= fragments.size()) return;
        fragments.remove(position);
        titles.remove(position);
        notifyItemRemoved(position);
    }

    // 替换 Fragment
    public void replaceFragment(int position, Fragment fragment) {
        if (position < 0 || position >= fragments.size()) return;
        fragments.set(position, fragment);
        notifyItemChanged(position);
    }

    // 如果有 TabLayout 需要标题
    public String getTitle(int position) {
        return titles.get(position);
    }
}
