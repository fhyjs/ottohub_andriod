package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_TYPE;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;
import static org.eu.hanana.reimu.ottohub_andriod.util.UiUtil.shareText;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.collection.CollectionResult;
import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.engagement.EngagementResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyAppApplicationLike;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.SearchActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.service.CopyService;
import org.eu.hanana.reimu.ottohub_andriod.service.DownloadVideoForegroundService;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.IScrollTopChecker;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClipboardUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.TouchInterceptFrameLayout;

import java.util.Arrays;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoDescribeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@UnstableApi
public class VideoDescribeFragment extends BaseFragment {
    protected TouchInterceptFrameLayout frameLayout;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_VDATA = "param1";

    private VideoResult vData;
    private View view;
    private CollectionResult collectionResult;

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
    public VideoPlayerActivity videoPlayerActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vData = new Gson().fromJson( getArguments().getString(ARG_VDATA),VideoResult.class);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        frameLayout=view.findViewById(R.id.fragment_container);
        ((TextView) view.findViewById(R.id.video_title)).setText(vData.title);
        ((TextView) view.findViewById(R.id.video_desc_text)).setText(vData.intro);
        ((TextView) view.findViewById(R.id.username)).setText(vData.username);
        ((TextView) view.findViewById(R.id.tvInfo)).setText(vData.userintro);
        view.findViewById(R.id.clAuthorInfo).setBackgroundColor(Color.TRANSPARENT);
        TextView vidInfo = view.findViewById(R.id.tvIntro);
        vidInfo.setText(getString(R.string.video_card_info_short,vData.view_count,vData.like_count,vData.favorite_count)+" (ov"+vData.vid+")");
        TextView vidTime = view.findViewById(R.id.tvDetail);
        vidTime.setText(vData.time);
        Glide.with(view.getContext())
                .load(vData.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .circleCrop()
                .into((ImageView) view.findViewById(R.id.ivAvatar));
        view.findViewById(R.id.clAuthorInfo).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,vData.uid);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        // 默认加载第一个 Fragment
        VideoListFragment videoListFragment = VideoListFragment.newInstance();
        videoListFragment.videosInRow=1;
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,videoListFragment )
                .commit();


        updateActionBtns();
        view.findViewById(R.id.btn_like).setOnClickListener(v -> {
            if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getActivity(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyAppApplicationLike.getInstance().getOttohubApi().getEngagementApi().like_video(vData.vid);
                ApiUtil.throwApiError(engagementResult);
                vData.like_count=engagementResult.like_count;
                vData.if_like=engagementResult.if_like;
                getActivity().runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            thread.start();
        });
        view.findViewById(R.id.btn_favourite).setOnClickListener(v -> {
            if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getContext(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyAppApplicationLike.getInstance().getOttohubApi().getEngagementApi().favorite_video(vData.vid);
                ApiUtil.throwApiError(engagementResult);
                vData.favorite_count=engagementResult.favorite_count;
                vData.if_favorite=engagementResult.if_favorite;
                getActivity().runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            thread.start();
        });
        view.findViewById(R.id.btn_report).setOnClickListener(v -> {
            if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getContext(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EmptyResult emptyResult = MyAppApplicationLike.getInstance().getOttohubApi().getModerationApi().report_video(vData.vid);
                ApiUtil.throwApiError(emptyResult);
                getActivity().runOnUiThread(()->{
                    AlertUtil.showMsg(getContext(), getString(R.string.report), getString(R.string.ok)).show();
                });
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            AlertUtil.showYesNo(getContext(), getString(R.string.report), getString(R.string.issure), (dialog, which) -> thread.start(),null).show();
        });
        view.findViewById(R.id.btn_download).setOnClickListener(v -> {
            getActivity().startService(DownloadVideoForegroundService.createIntent(vData.vid,getContext()));
        });
        LinearLayout tagsArea = view.findViewById(R.id.llTagsArea);
        Arrays.stream(vData.tag.split("#")).skip(1).forEach(tag -> {
           var btn = new MaterialButton(getContext());
           btn.setText(tag);
           btn.setTag("themed");
            // 添加间距
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 0, 16, 0);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> {
                    // 创建 Intent
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    // 添加额外数据（可选）
                    intent.putExtra(ARG_TYPE, TYPE_VIDEO);
                    intent.putExtra(SearchActivity.ARG_DATA, tag);
                    // 启动 Activity
                    startActivity(intent); // 简单启动
            });
           tagsArea.addView(btn);
        });
        ThemeUtil.apply(tagsArea);
        view.findViewById(R.id.btn_share).setOnClickListener(v -> {
            var txt = v.getContext().getString(R.string.share_content,vData.title,"https://m.ottohub.cn/v/"+vData.vid);
            ClipboardUtil.copyToClipboard(v.getContext(),txt);
            shareText(v.getContext(),txt);
        });
        view.findViewById(R.id.btn_download).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CopyService.class);
            intent.putExtra("uri", Uri.parse("content://org.eu.hanana.reimu.ottohub_andriod.provider.download/video?vid="+vData.vid));
            intent.putExtra("fileName", "video_"+vData.vid+".zip");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 必须用 startForegroundService
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }
            Toast.makeText(getContext(),R.string.notise_msg,Toast.LENGTH_SHORT).show();
        });

        var onTouchListener = new TouchInterceptFrameLayout.OnTouchListener() {
            private androidx.fragment.app.Fragment frag;
            private float lastY = 0;  // 记录上一次的 Y 坐标
            @Override
            public void onTouch(MotionEvent ev) {
                if (ev.getAction()==MotionEvent.ACTION_DOWN){
                    frag = getChildFragmentManager().findFragmentById(R.id.fragment_container);
                    lastY = ev.getY();  // 初始化上一次位置
                }else if (ev.getAction()==MotionEvent.ACTION_UP) {
                    frameLayout.setInterceptMove(true);
                }else {
                    float currentY = ev.getY();
                    float deltaY = currentY - lastY; // Y 方向变化量
                    lastY = currentY;  // 更新上一次位置
                    if (frag instanceof IScrollTopChecker){
                        var stc = ((IScrollTopChecker) frag);
                        if (stc.atTop()){
                            frameLayout.setInterceptMove(deltaY > 0);
                        }else {

                        }
                    }
                }
            }
        };
        frameLayout.setInterceptMove(true);
        frameLayout.setTouchListener(onTouchListener);
        frameLayout.setInterceptTouchListener(onTouchListener);
        view.post(()->{
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(frameLayout.getWidth(), (int) (view.getHeight()*0.7f)));
        });
        Thread thread = new Thread(this::fetchCollectionData);
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
        thread.start();
    }

    private void fetchCollectionData() {
        this.collectionResult = ApiUtil.getAppApi().getCollectionApi().get_video_collection(vData.vid);
        if (!collectionResult.isSuccess() &&!collectionResult.getMessage().contains("video_not_in_collection")) {
            ApiUtil.throwApiError(collectionResult);
            collectionResult=null;
            return;
        }
        if (collectionResult.getMessage()!=null&&collectionResult.getMessage().contains("video_not_in_collection")){
            collectionResult=null;
            return;
        }
        getActivity().runOnUiThread(this::renderCollectionData);
    }

    private void renderCollectionData() {
        if (collectionResult==null||videoPlayerActivity==null) return;
        videoPlayerActivity.dynamicFragmentAdapter.addFragment(CollectionFragment.newInstance(collectionResult.collection,vData.uid),getString(R.string.collection)+" "+collectionResult.collection);

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