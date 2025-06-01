package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.lib.ottohub.api.profile.ProfileResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClassUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ProfileUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import lombok.Getter;


public class ProfileFragment extends Fragment {
    public static final String Arg_Uid = "uid";
    protected ImageView ivAvatar;
    protected TextView tvInfo;
    protected TextView tvUsername,tvVideoCount,tvBlogCount,tvFollowing,tvFollower;
    protected ProfileResult userResult;
    protected UserResult userDataResult;
    protected LinearLayout llButtonPanel;
    protected Button btnFollow;
    @Getter
    protected int uid;
    @Getter
    protected boolean self =false;

    public ProfileFragment() {
        // Required empty public constructor
    }
    public static ProfileFragment newInstance(@Nullable Integer uid) {
        ProfileFragment fragment = new ProfileFragment();
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
        }else {
            userDataResult=MyApp.getInstance().getOttohubApi().getUserApi().get_user_detail(uid);
            userResult=new ProfileResult();
            ClassUtil.copyFields(ProfileResult.class,UserResult.class,userResult,userDataResult,false);
        }
    }
    protected void init() {
        this.self = uid==Integer.parseInt(MyApp.getInstance().getOttohubApi().getLoginResult().uid);
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
        Glide.with(getContext())
                .load(isSelf()?MyApp.getInstance().getOttohubApi().getLoginResult().avatar_url:userDataResult.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
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

        if (isSelf()){
            btnFollow.setText(R.string.narcissism);
            btnFollow.setOnClickListener(v -> {
                AlertUtil.showMsg(getContext(),getString(R.string.narcissism),getString(R.string.narcissism_msg)).show();
            });
        }
    }
}