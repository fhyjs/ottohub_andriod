package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static android.view.View.VISIBLE;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ACTION_BY_USER;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;

import org.eu.hanana.reimu.lib.ottohub.api.following.FollowStatusResult;
import org.eu.hanana.reimu.lib.ottohub.api.profile.ProfileResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BaseActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.FragActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ImageViewActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.MessageActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.IScrollTopChecker;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.settings.SettingsFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClassUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ProfileUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.TouchInterceptFrameLayout;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ui.BlurTransformation;
import org.eu.hanana.reimu.ottohub_andriod.util.ui.ColorOverlayTransformation;
import org.eu.hanana.reimu.ottohub_andriod.util.ui.HardwareToSoftwareTransformation;

import java.util.Arrays;
import java.util.Locale;

import lombok.Getter;


public class ProfileFragment extends BaseFragment {
    public static final String Arg_Uid = "uid";
    protected ImageView ivAvatar;
    protected TextView tvInfo;
    protected TextView tvUsername,tvVideoCount,tvBlogCount,tvFollowing,tvFollower;
    protected ProfileResult userResult;
    protected UserResult userDataResult;
    protected LinearLayout llButtonPanel;
    protected TouchInterceptFrameLayout frameLayout;
    protected Button btnFollow,btnVid,btnBlog;
    @Getter
    protected int uid;
    @Getter
    protected boolean self =false;
    protected FollowStatusResult followStatus;
    private boolean login=true;
    protected LinearLayout pageBtnArea;
    protected TextView tvIntro;
    protected TextView tvDetail;
    private View view;

    public ProfileFragment() {
        // Required empty public constructor
    }
    public static Fragment newInstance(@Nullable Integer uid) {
        Fragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        if (uid!=null)
            args.putInt(Arg_Uid,uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(Arg_Uid)){
                uid=getArguments().getInt(Arg_Uid);
            }else {
                uid= Integer.parseInt(MyApp.getInstance().getOttohubApi().getLoginResult().uid);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_profile, container, false);
        return inflate;
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
        frameLayout=view.findViewById(R.id.fragment_container);
        btnVid=view.findViewById(R.id.btnVideo);
        btnBlog=view.findViewById(R.id.btnBlog);
        pageBtnArea=view.findViewById(R.id.video_type_button_area);
        tvIntro=view.findViewById(R.id.tvIntro);
        tvDetail=view.findViewById(R.id.tvDetail);
        this.view=view;

        Thread thread = new Thread(()->{
            init();
            getActivity().runOnUiThread(this::initUI);
        });
        thread.setUncaughtExceptionHandler((t, e) -> getActivity().runOnUiThread(()->{
            AlertUtil.showError(getContext(),e.toString());
        }));
        thread.start();
    }
    protected void fetchData() throws Exception{
        if (isSelf()){
            var result= MyApp.getInstance().getOttohubApi().getProfileApi().user_profile();
            userDataResult= MyApp.getInstance().getOttohubApi().getProfileApi().user_data();
            userResult=result.profile;
            userResult.status=result.status;
            userResult.message=result.getMessage();
            ApiUtil.fetchMsgCount();
        }else {
            userDataResult=MyApp.getInstance().getOttohubApi().getUserApi().get_user_detail(uid);
            userResult=new ProfileResult();
            ClassUtil.copyFields(ProfileResult.class,UserResult.class,userResult,userDataResult,false);
        }
        followStatus = MyApp.getInstance().getOttohubApi().getFollowingApi().follow_status(uid);
    }

    @Override
    public void onDestroy() {
        getActivity().setTitle(R.string.app_name);
        super.onDestroy();
    }

    protected void init() {
        if (MyApp.getInstance().getOttohubApi().getLoginResult()==null){
            self=false;
            login=false;
        }else {
            this.self = uid == Integer.parseInt(MyApp.getInstance().getOttohubApi().getLoginResult().uid);
        }
        try {
            fetchData();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (!userResult.isSuccess()){
            throw new RuntimeException("Error getting userdata:"+userResult.getMessage());
        }
        if (!userDataResult.isSuccess()){
            throw new RuntimeException("Error getting userinfo:"+userDataResult.getMessage());
        }
    }
    protected void initUI() {
        if (getActivity()==null) return;
        Glide.with(getContext())
                .load(isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().avatar_url:userDataResult.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .circleCrop()
                .into(ivAvatar);
        tvUsername.setText(userResult.username);
        var exp = ProfileUtil.exp_show(userResult.experience);
        tvInfo.setText(String.format(Locale.getDefault(),"UID:%d %s:%d/%d",userResult.uid,getString(R.string.exp),userResult.experience,exp.nextExp));
        var expBtn = ProfileUtil.makeButton(getContext(),exp.level);
        expBtn.setTextColor(0xff000000);
        expBtn.setBackgroundColor(exp.color);
        llButtonPanel.addView(expBtn);
        Arrays.stream(userResult.honour.split(",")).forEach(s -> llButtonPanel.addView(ProfileUtil.makeButton(getContext(),s)));

        tvVideoCount.setText(String.valueOf(userDataResult.video_num));
        tvBlogCount.setText(String.valueOf(userDataResult.blog_num));
        tvFollowing.setText(String.valueOf(userDataResult.followings_count));
        tvFollower.setText(String.valueOf(userDataResult.fans_count));
        getActivity().setTitle(String.format(Locale.getDefault(),"%s's %s",userResult.username,getString(R.string.profile)));
        if (!login){
            updateFollowStatus(-1);
        }else {
            updateFollowStatus(followStatus.follow_status);
        }
        addPageBtn();
        for (int i = 0; i < pageBtnArea.getChildCount(); i++) {
            var child = pageBtnArea.getChildAt(i);
            child.setOnClickListener(buttonClicked -> setPage((Button) buttonClicked));
        }
        setPage((Button) pageBtnArea.getChildAt(0));

        tvIntro.setText(userResult.intro);
        tvDetail.setText(String.format(Locale.getDefault(),"%s:%s %s:%s",getString(R.string.sex),userResult.sex,getString(R.string.register_time),userResult.time));
        addMenu();

        tvFollower.setOnClickListener(v -> {
            var intent = new Intent(getContext(), FragActivity.class);
            var data = new Bundle();
            data.putString(FragActivity.ARG_FRAG_CLASS, UserListFragment.class.getName());
            data.putString(FragActivity.ARG_TITLE, getString(R.string.followers));
            var fragData = new Bundle();
            fragData.putString(UserListFragment.ARG_ACTION,UserListFragment.TYPE_FOLLOWER);
            fragData.putString(UserListFragment.ARG_DATA, String.valueOf(uid));
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
            fragData.putString(UserListFragment.ARG_DATA, String.valueOf(uid));
            data.putBundle(FragActivity.ARG_DATA,fragData);
            intent.putExtras(data);
            startActivity(intent);
        });
        var colorMask = ThemeUtil.getTheme(getContext()).getColorBackground();
        //将封面作为卡片背景
        Glide.with(this)
                .load(isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().cover_url:userDataResult.cover_url)
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
        //滑动控制
        TouchInterceptFrameLayout.OnTouchListener onTouchListener = new TouchInterceptFrameLayout.OnTouchListener() {
            private Fragment frag;
            private float deltaY;//小于零上划，大于零下拉
            private final View pta = view.findViewById(R.id.profile_top_area);
            private int originalHeight = 0;
            private float downY = 0;
            private final int minHeight = 0; // px，可以换成 dp 转换
            private int maxHeight = 0; // 可设置上限避免爆炸拉伸
            private final int touchSlop = 8;   // 忽略小于8px的滑动
            private boolean visible=true;
            @Override
            public void onTouch(MotionEvent ev) {
                ViewGroup.LayoutParams lp = pta.getLayoutParams();

                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = ev.getY();
                        originalHeight = pta.getHeight();
                        // 动态获取最大高度（只获取一次）
                        if (maxHeight == 0 || maxHeight < originalHeight) {
                            maxHeight = originalHeight;
                        }
                        frag = getChildFragmentManager().findFragmentById(R.id.fragment_container);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        deltaY = ev.getY() - downY;

                        // 滑动小于 touchSlop 忽略（防止抖动）
                        if (Math.abs(deltaY) < touchSlop) break;
                        int newHeight = (int) (originalHeight + deltaY);

                        // 限制高度范围
                        newHeight = Math.max(minHeight, Math.min(maxHeight, newHeight));

                        // 只有变化时才设置，减少 requestLayout 触发
                        if (newHeight != lp.height && Math.abs(newHeight - lp.height) > 1) {
                            lp.height = newHeight;
                            pta.setLayoutParams(lp);
                        }
                        if (newHeight<=minHeight){
                            pta.setVisibility(View.GONE);
                            frameLayout.setInterceptMove(false);
                        }else {
                            if (frag instanceof IScrollTopChecker){
                                var top = ((IScrollTopChecker) frag).atTop();
                                //frameLayout.setInterceptMove(false);
                                if (!top) break;
                            }
                            pta.setVisibility(VISIBLE);
                            frameLayout.setInterceptMove(true);
                            if (newHeight>=maxHeight){
                                frameLayout.setInterceptMove(false);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        ValueAnimator animator;
                        if(lp.height<0||(deltaY==0&&pta.getHeight()==maxHeight)){
                            animator = ValueAnimator.ofInt(pta.getHeight(), 0);
                            animator.setDuration(300); // 动画时长 1 秒
                            animator.addUpdateListener(animation -> {
                                lp.height= (int) animation.getAnimatedValue();
                                pta.setLayoutParams(lp);
                            });
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (lp.height<maxHeight*0.5) {
                                        pta.setVisibility(View.GONE);
                                        visible=false;
                                        frameLayout.setInterceptMove(false);
                                    }else {
                                        visible=true;
                                        frameLayout.setInterceptMove(true);
                                    }
                                }
                            });
                            animator.start();
                            return;
                        }
                        if (deltaY==0) break;
                        if (visible) {
                            if (lp.height>=maxHeight) break;
                            animator = ValueAnimator.ofInt(lp.height, 0);
                        }else {
                            if (lp.height<=minHeight) break;
                            animator = ValueAnimator.ofInt(lp.height, maxHeight);
                        }
                        animator.setDuration(300); // 动画时长 1 秒
                        animator.addUpdateListener(animation -> {
                            int value = (int) animation.getAnimatedValue();
                            lp.height= value;
                            pta.setLayoutParams(lp);
                        });
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (lp.height<maxHeight*0.5) {
                                    pta.setVisibility(View.GONE);
                                    visible=false;
                                    frameLayout.setInterceptMove(false);
                                }else {
                                    visible=true;
                                    frameLayout.setInterceptMove(true);
                                }
                            }
                        });
                        pta.setVisibility(VISIBLE);
                        animator.start();

                        break;
                }
            }
        };
        frameLayout.setInterceptMove(true);
        frameLayout.setTouchListener(onTouchListener);
        frameLayout.setInterceptTouchListener(onTouchListener);
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
                    var number =ApiUtil.getNewMegCount();
                    badgeTextView.setText(number>99?"99+":String.valueOf(number));   // 角标数字
                    badgeTextView.setVisibility(ApiUtil.getNewMegCount()>0? VISIBLE:View.GONE);
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
                    var isExpanded = view.findViewById(R.id.profile_top_area).getVisibility()== VISIBLE;
                    if (isExpanded){
                        menuItem.setIcon(R.drawable.arrow_downward_24dp);
                        UiUtil.slideUp(view.findViewById(R.id.profile_top_area));
                    }else {
                        menuItem.setIcon(R.drawable.arrow_upward_24dp);
                        UiUtil.slideDown(view.findViewById(R.id.profile_top_area));
                    }
                    return true;
                } else if (menuItem.getItemId() == R.id.btn_view_avatar) {
                    ImageViewActivity.start(getContext(),isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().avatar_url:userDataResult.avatar_url);
                } else if (menuItem.getItemId() == R.id.btn_view_cover) {
                    ImageViewActivity.start(getContext(),isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().cover_url:userDataResult.cover_url);
                }
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);
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
    public void setPage(Button buttonClicked){
        buttonClicked.setEnabled(false);
        for (int i = 0; i < pageBtnArea.getChildCount(); i++) {
            var child = pageBtnArea.getChildAt(i);
            if (child!=buttonClicked){
                child.setEnabled(true);
            }
        }
        if (buttonClicked.getText().equals(getString(R.string.videos))){
            var listFragment = VideoListFragment.newInstance();
            listFragment.getArguments().putInt(Arg_Uid,uid);
            listFragment.getArguments().putString(ARG_ACTION,ACTION_BY_USER);
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, listFragment).commit();
        }else if (buttonClicked.getText().equals(getString(R.string.blogs))){
            var listFragment = BlogListFragment.newInstance();
            listFragment.getArguments().putInt(Arg_Uid,uid);
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, listFragment).commit();
        }else if (buttonClicked.getText().equals(getString(R.string.history))){
            var listFragment = VideoListFragment.newInstance();
            listFragment.getArguments().putInt(Arg_Uid,uid);
            listFragment.getArguments().putString(ARG_ACTION,VideoListFragment.ACTION_HISTORY);
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, listFragment).commit();
        }else if (buttonClicked.getText().equals(getString(R.string.settings))){
            var fragment = new SettingsFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }
    public void doFollow(){
        Thread thread = new Thread(()->{
            followStatus= MyApp.getInstance().getOttohubApi().getFollowingApi().follow(uid);
            getActivity().runOnUiThread(()->{
                tvFollower.setText(String.valueOf(followStatus.new_fans_count));
                updateFollowStatus(followStatus.follow_status);
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