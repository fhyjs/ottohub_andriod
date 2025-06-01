package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.engagement.EngagementResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoDescribeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoDescribeFragment extends androidx.fragment.app.Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_VDATA = "param1";

    private VideoResult vData;
    private View view;

    public VideoDescribeFragment() {
        // Required empty public constructor
    }


    public static VideoDescribeFragment newInstance(VideoResult data) {
        VideoDescribeFragment fragment = new VideoDescribeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VDATA, new Gson().toJson(data));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vData = new Gson().fromJson( getArguments().getString(ARG_VDATA),VideoResult.class);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        ((TextView) view.findViewById(R.id.video_title)).setText(vData.title);
        ((TextView) view.findViewById(R.id.video_desc_text)).setText(vData.intro);
        ((TextView) view.findViewById(R.id.username)).setText(vData.username);
        ((TextView) view.findViewById(R.id.tvInfo)).setText(vData.userintro);
        ((MaterialCardView) view.findViewById(R.id.clAuthorInfo)).removeView(view.findViewById(R.id.tvIntro));
        ((MaterialCardView) view.findViewById(R.id.clAuthorInfo)).removeView(view.findViewById(R.id.tvDetail));
        Glide.with(view.getContext())
                .load(vData.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into((ImageView) view.findViewById(R.id.ivAvatar));
        view.findViewById(R.id.clAuthorInfo).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,vData.uid);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        // 默认加载第一个 Fragment
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, VideoListFragment.newInstance())
                .commit();


        updateActionBtns();
        view.findViewById(R.id.btn_like).setOnClickListener(v -> {
            if (MyApp.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getActivity(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyApp.getInstance().getOttohubApi().getEngagementApi().like_video(vData.vid);
                ApiUtil.throwApiError(engagementResult);
                vData.like_count=engagementResult.like_count;
                vData.if_like=engagementResult.if_like;
                getActivity().runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            thread.start();
        });
        view.findViewById(R.id.btn_favourite).setOnClickListener(v -> {
            if (MyApp.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getContext(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyApp.getInstance().getOttohubApi().getEngagementApi().favorite_video(vData.vid);
                ApiUtil.throwApiError(engagementResult);
                vData.favorite_count=engagementResult.favorite_count;
                vData.if_favorite=engagementResult.if_favorite;
                getActivity().runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            thread.start();
        });
        view.findViewById(R.id.btn_report).setOnClickListener(v -> {
            if (MyApp.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getContext(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EmptyResult emptyResult = MyApp.getInstance().getOttohubApi().getModerationApi().report_video(vData.vid);
                ApiUtil.throwApiError(emptyResult);
                getActivity().runOnUiThread(()->{
                    AlertUtil.showMsg(getContext(), getString(R.string.report), getString(R.string.ok)).show();
                });
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            AlertUtil.showYesNo(getContext(), getString(R.string.report), getString(R.string.issure), (dialog, which) -> thread.start(),null).show();
        });
    }
    private void updateActionBtns() {
        ((TextView) view.findViewById(R.id.btn_like)).setText(String.format(Locale.getDefault(),"%d%s",vData.like_count,getString(R.string.like)));
        ((TextView) view.findViewById(R.id.btn_favourite)).setText(String.format(Locale.getDefault(),"%d%s",vData.favorite_count,getString(R.string.favourite)));
        if (vData.if_like==1){
            ((MaterialButton) view.findViewById(R.id.btn_like)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.thumb_up_24dp_fill));
        }else {
            ((MaterialButton) view.findViewById(R.id.btn_like)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.thumb_up_24dp));
        }
        if (vData.if_favorite==1){
            ((MaterialButton) view.findViewById(R.id.btn_favourite)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.kitchen_24dp_fill));
        }else {
            ((MaterialButton) view.findViewById(R.id.btn_favourite)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.kitchen_24dp));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_describe, container, false);
    }
}