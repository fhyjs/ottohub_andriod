package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ACTION_BY_USER;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.lib.ottohub.api.following.FollowStatusResult;
import org.eu.hanana.reimu.lib.ottohub.api.profile.ProfileResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.MyAppApplicationLike;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.FragmentAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.settings.SettingsFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClassUtil;

import lombok.Getter;


public class ProfileFragment2 extends BaseFragment {
    public static final String Arg_Uid = "uid";
    @Getter
    protected int uid;
    @Getter
    protected boolean self =false;
    private View view;
    public UserResult userDataResult;
    public ProfileResult userResult;
    public FollowStatusResult followStatus;
    public boolean login=true;
    private RecyclerView recyclerView;
    private ConcatAdapter adapter;
    private FragmentAdapter contentAdapter;

    public ProfileFragment2() {
        // Required empty public constructor
    }
    public static ProfileFragment2 newInstance(@Nullable Integer uid) {
        ProfileFragment2 fragment = new ProfileFragment2();
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
                uid= Integer.parseInt(MyAppApplicationLike.getInstance().getOttohubApi().getLoginResult().uid);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_profile2, container, false);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
            var result= MyAppApplicationLike.getInstance().getOttohubApi().getProfileApi().user_profile();
            userDataResult= MyAppApplicationLike.getInstance().getOttohubApi().getProfileApi().user_data();
            userResult=result.profile;
            userResult.status=result.status;
            userResult.message=result.getMessage();
            ApiUtil.fetchMsgCount();
        }else {
            userDataResult=MyAppApplicationLike.getInstance().getOttohubApi().getUserApi().get_user_detail(uid);
            userResult=new ProfileResult();
            ClassUtil.copyFields(ProfileResult.class,UserResult.class,userResult,userDataResult,false);
        }
        followStatus = MyAppApplicationLike.getInstance().getOttohubApi().getFollowingApi().follow_status(uid);
    }

    @Override
    public void onDestroy() {
        getActivity().setTitle(R.string.app_name);
        super.onDestroy();
    }

    protected void init() {
        if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginResult()==null){
            self=false;
            login=false;
        }else {
            this.self = uid == Integer.parseInt(MyAppApplicationLike.getInstance().getOttohubApi().getLoginResult().uid);
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
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new ConcatAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        FrameLayout header = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.video_list_header_wrapper, recyclerView, false);
        adapter.addAdapter(new FragmentAdapter(header,this,new Profile2TopFragment(this)));
        recyclerView.setAdapter(adapter);
    }
    public void setPage(Button buttonClicked, LinearLayout pageBtnArea){
        buttonClicked.setEnabled(false);
        for (int i = 0; i < pageBtnArea.getChildCount(); i++) {
            var child = pageBtnArea.getChildAt(i);
            if (child!=buttonClicked){
                child.setEnabled(true);
            }
        }
        Fragment fragment = null;
        if (buttonClicked.getText().equals(getString(R.string.videos))){
            fragment = VideoListFragment.newInstance();
            fragment.getArguments().putInt(Arg_Uid,uid);
            fragment.getArguments().putString(ARG_ACTION,ACTION_BY_USER);
        }else if (buttonClicked.getText().equals(getString(R.string.blogs))){
            fragment= BlogListFragment.newInstance();
            fragment.getArguments().putInt(Arg_Uid,uid);
        }else if (buttonClicked.getText().equals(getString(R.string.history))){
            fragment = VideoListFragment.newInstance();
            fragment.getArguments().putInt(Arg_Uid,uid);
            fragment.getArguments().putString(ARG_ACTION,VideoListFragment.ACTION_HISTORY);
        }else if (buttonClicked.getText().equals(getString(R.string.settings))){
            fragment = new SettingsFragment();
        }
        if (fragment!=null){
            if (contentAdapter!=null) adapter.removeAdapter(contentAdapter);
            FrameLayout header = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.video_list_header_wrapper, recyclerView, false);
            contentAdapter = new FragmentAdapter(header,this,fragment);
            adapter.addAdapter(contentAdapter);
            recyclerView.scrollToPosition(0);
        }
    }
}