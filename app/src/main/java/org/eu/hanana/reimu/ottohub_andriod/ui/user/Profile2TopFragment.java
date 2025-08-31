package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static org.eu.hanana.reimu.ottohub_andriod.ui.user.ProfileFragment2.Arg_Uid;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ACTION_BY_USER;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;

import org.eu.hanana.reimu.lib.ottohub.api.profile.ProfileResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.FragActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ImageViewActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.MessageActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.settings.SettingsFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ProfileUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ui.BlurTransformation;
import org.eu.hanana.reimu.ottohub_andriod.util.ui.ColorOverlayTransformation;
import org.eu.hanana.reimu.ottohub_andriod.util.ui.HardwareToSoftwareTransformation;

import java.util.Arrays;
import java.util.Locale;

public class Profile2TopFragment extends BaseFragment {
    private final ProfileFragment2 pf2;
    protected ImageView ivAvatar;
    protected TextView tvInfo;
    protected TextView tvUsername,tvVideoCount,tvBlogCount,tvFollowing,tvFollower;
    protected LinearLayout llButtonPanel;
    protected Button btnFollow,btnVid,btnBlog;
    protected LinearLayout pageBtnArea;
    protected TextView tvIntro;
    protected TextView tvDetail;
    private View view;
    public Profile2TopFragment(ProfileFragment2 profileFragment2){
        this.pf2 = profileFragment2;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_top_profile2,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAvatar=view.findViewById(R.id.ivAvatar);
        tvInfo=view.findViewById(R.id.tvInfo);
        tvUsername=view.findViewById(R.id.username);
        llButtonPanel=view.findViewById(R.id.buttonPanel);
        tvVideoCount=view.findViewById(R.id.tvVideos);
        tvBlogCount=view.findViewById(R.id.tvBlogs);
        tvFollower=view.findViewById(R.id.tvFollowers);
        tvFollowing=view.findViewById(R.id.tvFollowings);
        btnFollow=view.findViewById(R.id.btnFollow);
        btnVid=view.findViewById(R.id.btnVideo);
        btnBlog=view.findViewById(R.id.btnBlog);
        pageBtnArea=view.findViewById(R.id.video_type_button_area);
        tvIntro=view.findViewById(R.id.tvIntro);
        tvDetail=view.findViewById(R.id.tvDetail);
        this.view=view;
        initUI();
    }

    private void initUI() {
        if (getActivity()==null) return;
        Glide.with(getContext())
                .load(isSelf()? MyApp.getInstance().getOttohubApi().getLoginResult().avatar_url:pf2.userDataResult.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .circleCrop()
                .into(ivAvatar);
        tvUsername.setText(pf2.userResult.username);
        var exp = ProfileUtil.exp_show(pf2.userResult.experience);
        tvInfo.setText(String.format(Locale.getDefault(),"UID:%d %s:%d/%d",pf2.userResult.uid,getString(R.string.exp),pf2.userResult.experience,exp.nextExp));
        var expBtn = ProfileUtil.makeButton(getContext(),exp.level);
        expBtn.setTextColor(0xff000000);
        expBtn.setBackgroundColor(exp.color);
        llButtonPanel.addView(expBtn);
        Arrays.stream(pf2.userResult.honour.split(",")).forEach(s -> llButtonPanel.addView(ProfileUtil.makeButton(getContext(),s)));

        tvVideoCount.setText(String.valueOf(pf2.userDataResult.video_num));
        tvBlogCount.setText(String.valueOf(pf2.userDataResult.blog_num));
        tvFollowing.setText(String.valueOf(pf2.userDataResult.followings_count));
        tvFollower.setText(String.valueOf(pf2.userDataResult.fans_count));
        getActivity().setTitle(String.format(Locale.getDefault(),"%s's %s",pf2.userResult.username,getString(R.string.profile)));
        if (!pf2.login){
            updateFollowStatus(-1);
        }else {
            updateFollowStatus(pf2.followStatus.follow_status);
        }
        addPageBtn();
        for (int i = 0; i < pageBtnArea.getChildCount(); i++) {
            var child = pageBtnArea.getChildAt(i);
            child.setOnClickListener(buttonClicked -> pf2.setPage((Button) buttonClicked,pageBtnArea));
        }
        pf2.setPage((Button) pageBtnArea.getChildAt(0),pageBtnArea);

        tvIntro.setText(pf2.userResult.intro);
        tvDetail.setText(String.format(Locale.getDefault(),"%s:%s %s:%s",getString(R.string.sex),pf2.userResult.sex,getString(R.string.register_time),pf2.userResult.time));
        addMenu();

        tvFollower.setOnClickListener(v -> {
            var intent = new Intent(getContext(), FragActivity.class);
            var data = new Bundle();
            data.putString(FragActivity.ARG_FRAG_CLASS, UserListFragment.class.getName());
            data.putString(FragActivity.ARG_TITLE, getString(R.string.followers));
            var fragData = new Bundle();
            fragData.putString(UserListFragment.ARG_ACTION,UserListFragment.TYPE_FOLLOWER);
            fragData.putString(UserListFragment.ARG_DATA, String.valueOf(pf2.uid));
            data.putBundle(FragActivity.ARG_DATA,fragData);
            intent.putExtras(data);
            startActivity(intent);
        });
        tvFollowing.setOnClickListener(v -> {
            var intent = new Intent(getContext(), FragActivity.class);
            var data = new Bundle();
            data.putString(FragActivity.ARG_FRAG_CLASS, UserListFragment.class.getName());
            data.putString(FragActivity.ARG_TITLE, getString(R.string.followings));
            var fragData = new Bundle();
            fragData.putString(UserListFragment.ARG_ACTION,UserListFragment.TYPE_FOLLOWING);
            fragData.putString(UserListFragment.ARG_DATA, String.valueOf(pf2.uid));
            data.putBundle(FragActivity.ARG_DATA,fragData);
            intent.putExtras(data);
            startActivity(intent);
        });
        var colorMask = ThemeUtil.getTheme(getContext()).getColorBackground();
        //将封面作为卡片背景
        Glide.with(this)
                .load(isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().cover_url:pf2.userDataResult.cover_url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .apply(RequestOptions.bitmapTransform( new MultiTransformation<>(
                        new HardwareToSoftwareTransformation(),
                        new BlurTransformation(getContext(), 10),
                        new HardwareToSoftwareTransformation(),
                        new ColorOverlayTransformation(ColorUtils.setAlphaComponent(colorMask,0x6c))
                )))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        View topArea = view.findViewById(R.id.profile_top_area);
                        int width = topArea.getWidth();
                        int height = topArea.getHeight();

                        if (resource instanceof BitmapDrawable && width > 0 && height > 0) {
                            Bitmap srcBitmap = ((BitmapDrawable) resource).getBitmap();
                            Bitmap scaled = Bitmap.createScaledBitmap(srcBitmap, width, height, true);
                            topArea.setBackground(new BitmapDrawable(topArea.getResources(), scaled));
                        } else {
                            // fallback 原图
                            topArea.setBackground(resource);
                        }

                        view.findViewById(R.id.card_profile_top).setBackgroundColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }
    private void addMenu() {
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onMenuClosed(@NonNull Menu menu) {
                MenuProvider.super.onMenuClosed(menu);
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if (isSelf()){
                    Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.mail_24dp);
                    drawable.setTintList(ContextCompat.getColorStateList(getContext(),R.color.black));
                    menu.add(Menu.NONE,10,Menu.NONE,getString(R.string.mail)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
                menuInflater.inflate(R.menu.menu_action_profile,menu);
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                // 找到对应菜单项
                MenuItem menuItem = menu.findItem(10);
                if (menuItem!=null) {
                    // 创建自定义的角标视图
                    FrameLayout actionView = (FrameLayout) getLayoutInflater().inflate(R.layout.menu_item_badge, (ViewGroup) view.getRootView(), false);

                    menuItem.setActionView(actionView);
                    actionView.setOnClickListener(v -> {
                        onMenuItemSelected(menuItem);
                    });
                    // 更新角标数字
                    TextView badgeTextView = actionView.findViewById(R.id.badge_text_view);
                    badgeTextView.setText(String.valueOf(ApiUtil.getNewMegCount()));   // 角标数字
                    badgeTextView.setVisibility(ApiUtil.getNewMegCount()>0?View.VISIBLE:View.GONE);
                    ThemeUtil.apply(actionView);
                    actionView.setBackgroundColor(ThemeUtil.getTheme(getContext()).getColorActionBar());

                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId()==10){
                    Intent intent = new Intent(getContext(), MessageActivity.class);
                    startActivity(intent);
                    return true;
                }else if (menuItem.getItemId() == R.id.action_toggle) {
                    var isExpanded = view.findViewById(R.id.profile_top_area).getVisibility()==View.VISIBLE;
                    if (isExpanded){
                        menuItem.setIcon(R.drawable.arrow_downward_24dp);
                        UiUtil.slideUp(view.findViewById(R.id.profile_top_area));
                    }else {
                        menuItem.setIcon(R.drawable.arrow_upward_24dp);
                        UiUtil.slideDown(view.findViewById(R.id.profile_top_area));
                    }
                    return true;
                } else if (menuItem.getItemId() == R.id.btn_view_avatar) {
                    ImageViewActivity.start(getContext(),isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().avatar_url:pf2.userDataResult.avatar_url);
                } else if (menuItem.getItemId() == R.id.btn_view_cover) {
                    ImageViewActivity.start(getContext(),isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().cover_url:pf2.userDataResult.cover_url);
                }
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
    protected boolean isSelf(){
        return pf2.isSelf();
    }
    protected void addPageBtn() {
        if (isSelf()){
            pageBtnArea.addView(makeButton(getString(R.string.history)));
            pageBtnArea.addView(makeButton(getString(R.string.settings)));
        }
    }
    protected MaterialButton makeButton(String text){
        MaterialButton button = new MaterialButton(getContext());
        button.setText(text);
        button.setTag("themed");
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        if (params == null) {
            // 假设父布局是 LinearLayout，可以根据实际替换
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        int marginInDp = 3;
        float scale = button.getContext().getResources().getDisplayMetrics().density;
        int marginInPx = (int) (marginInDp * scale + 0.5f);

        params.setMargins(0, params.topMargin, marginInPx, params.bottomMargin);
        button.setLayoutParams(params);
        return button;
    }
    public void doFollow(){
        Thread thread = new Thread(()->{
            pf2.followStatus= MyApp.getInstance().getOttohubApi().getFollowingApi().follow(pf2.uid);
            getActivity().runOnUiThread(()->{
                tvFollower.setText(String.valueOf(pf2.followStatus.new_fans_count));
                updateFollowStatus(pf2.followStatus.follow_status);
            });
        });
        thread.setUncaughtExceptionHandler((t, e) -> getActivity().runOnUiThread(()->{
            AlertUtil.showError(getContext(),e.toString());
        }));
        thread.start();
    }
    protected void updateFollowStatus(int status){
        btnFollow.setOnClickListener(v -> doFollow());
        if (status==0){
            btnFollow.setText(R.string.narcissism);
            btnFollow.setOnClickListener(v -> {
                AlertUtil.showMsg(getContext(),getString(R.string.narcissism),getString(R.string.narcissism_msg)).show();
            });
        }else if (status==1){
            btnFollow.setText(R.string.follow);
        }else if (status==2){
            btnFollow.setText(R.string.defollowing);
        }else if (status==3){
            btnFollow.setText(R.string.refollow);
        }else if (status==4){
            btnFollow.setText(R.string.derefollow);
        }else {
            btnFollow.setText(R.string.not_login);
            btnFollow.setOnClickListener(v -> {
                AlertUtil.showMsg(getContext(),getString(R.string.follow),getString(R.string.not_login)).show();
            });
        }
    }
}
