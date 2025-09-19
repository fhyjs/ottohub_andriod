package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;

import java.util.List;

public class VideoPagerAdapter extends FragmentStateAdapter {

    private List<VideoResult> videoUrls;

    public VideoPagerAdapter(@NonNull FragmentActivity fa, List<VideoResult> urls) {
        super(fa);
        this.videoUrls = urls;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return VideoFragment.newInstance(videoUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return videoUrls.size();
    }
}
