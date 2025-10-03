package org.eu.hanana.reimu.ottohub_andriod.ui.banner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.eu.hanana.reimu.lib.ottohub.api.system.SlidesResult;
import org.eu.hanana.reimu.lib.ottohub.api.system.SlideshowResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.List;

public class BannerFragment extends Fragment {

    private ViewPager2 viewPager;
    private BannerAdapter adapter;
    public TabLayout tabLayout;
    private Handler handler = new Handler();
    private Runnable runnable;
    private View view;
    public List<SlidesResult>slidesResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.video_list_header, container, false);
        inflate.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (UiUtil.getAppWindowHeight(getActivity())*0.30f)));
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Thread thread = new Thread(() -> {
            SlideshowResult slideshow = ApiUtil.getAppApi().getSystemApi().slideshow();
            ApiUtil.throwApiError(slideshow);
            slidesResult=slideshow.slides;
            slidesResult.forEach(slidesResult1 -> {
                if (!slidesResult1.img_url.startsWith("http")){
                    slidesResult1.img_url="https://"+slidesResult1.img_url;
                }
            });
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!BannerFragment.this.isAdded()) {
                        mainHandler.post(this);
                        return;
                    }
                    start();
                }
            });
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
        thread.start();;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public void start() {
        if (!isAdded()) return; // Fragment 还没 attach，直接返回

        viewPager = view.findViewById(R.id.viewPagerBanner);
        tabLayout = view.findViewById(R.id.tabLayoutIndicator);


        adapter = new BannerAdapter(slidesResult,this);
        viewPager.setAdapter(adapter);

        // TabLayout + ViewPager2 指示器
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // 不用设置文字，圆点由 selector 控制
        }).attach();

        // 自动轮播
        runnable = new Runnable() {
            @Override
            public void run() {
                int next = (viewPager.getCurrentItem() + 1) % slidesResult.size();
                viewPager.setCurrentItem(next, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);

        // 绑定 TabLayout 与 ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            ImageView dot = new ImageView(requireContext());
            int size = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(position == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
            tab.setCustomView(dot);
        }).attach();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    ImageView dot = (ImageView) tabLayout.getTabAt(i).getCustomView();
                    if (dot != null) {
                        dot.setBackgroundResource(i == position ? R.drawable.dot_selected : R.drawable.dot_unselected);
                        dot.getBackground().setTint(ThemeUtil.getTheme(getContext()).getColorPrimary());
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}