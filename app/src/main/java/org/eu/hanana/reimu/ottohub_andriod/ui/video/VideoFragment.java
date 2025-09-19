package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_CENTER_TOP;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_MODE_ROLLING;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.DANMAKU_STYLE_NONE;
import static com.kuaishou.akdanmaku.data.DanmakuItemData.MERGED_TYPE_NORMAL;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.kuaishou.akdanmaku.DanmakuConfig;
import com.kuaishou.akdanmaku.data.DanmakuItemData;
import com.kuaishou.akdanmaku.data.DataSource;
import com.kuaishou.akdanmaku.ui.DanmakuPlayer;

import org.eu.hanana.reimu.lib.ottohub.api.danmaku.DanmakuListResult;
import org.eu.hanana.reimu.lib.ottohub.api.engagement.EngagementResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BottomFragmentContainer;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.UnlockedDanmakuRender;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {
    private VideoResult videoResult;
    private View view;
    private PlayerView videoSurface;
    private ExoPlayer mediaPlayer;
    private DanmakuPlayer danmakuPlayer;
    private DanmakuConfig danmakuConfig;
    private DanmakuListResult danmakuData;

    public VideoFragment() {
        // Required empty public constructor
    }


    public static VideoFragment newInstance(VideoResult videoResult) {
        VideoFragment fragment = new VideoFragment();
        fragment.videoResult=videoResult;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        ((TextView) view.findViewById(R.id.title)).setText(videoResult.title);
        ((TextView) view.findViewById(R.id.tv_name)).setText(videoResult.username);
        UiUtil.loadImgToImageView(view.findViewById(R.id.ivAvatar),videoResult.avatar_url);
        videoSurface = view.findViewById(R.id.video_view);
        view.findViewById(R.id.ll_userinfo).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,videoResult.uid);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        view.findViewById(R.id.btn_like).setOnClickListener(v -> {
            if (MyApp.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getActivity(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyApp.getInstance().getOttohubApi().getEngagementApi().like_video(videoResult.vid);
                ApiUtil.throwApiError(engagementResult);
                videoResult.like_count=engagementResult.like_count;
                videoResult.if_like=engagementResult.if_like;
                getActivity().runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            thread.start();
        });
        view.findViewById(R.id.btn_favorite).setOnClickListener(v -> {
            if (MyApp.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(getContext(),getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyApp.getInstance().getOttohubApi().getEngagementApi().favorite_video(videoResult.vid);
                ApiUtil.throwApiError(engagementResult);
                videoResult.favorite_count=engagementResult.favorite_count;
                videoResult.if_favorite=engagementResult.if_favorite;
                getActivity().runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
            thread.start();
        });
        view.findViewById(R.id.btn_detail).setOnClickListener(v -> {
            var target = new Intent(this.getContext(), VideoPlayerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(VideoPlayerActivity.KEY_VID,videoResult.vid);
            target.putExtras(bundle);
            startActivity(target);
        });
        view.findViewById(R.id.btn_comment).setOnClickListener(v -> {
            // 假设你有一个 Fragment：CommentFragment
            Fragment commentFragment = CommentFragmentBase.newInstance(videoResult.vid,0,CommentFragmentBase.TYPE_VIDEO);

            // 包装成 BottomSheet 并显示
            BottomFragmentContainer bottomSheet =
                    new BottomFragmentContainer(commentFragment, getString(R.string.comment));
            bottomSheet.setBackgroundColor(ThemeUtil.getTheme(getContext()).getColorBackground()); // 背景色
            bottomSheet.show(getChildFragmentManager(), "bottom_fragment");
        });
        updateActionBtns();
    }
    private void updateActionBtns() {
        ((TextView) view.findViewById(R.id.btn_like)).setText(String.format(Locale.getDefault(),"%d%s",videoResult.like_count,getString(R.string.like)));
        ((TextView) view.findViewById(R.id.btn_favorite)).setText(String.format(Locale.getDefault(),"%d%s",videoResult.favorite_count,getString(R.string.favourite)));
        if (videoResult.if_like==1){
            ((MaterialButton) view.findViewById(R.id.btn_like)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.thumb_up_24dp_fill));
        }else {
            ((MaterialButton) view.findViewById(R.id.btn_like)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.thumb_up_24dp));
        }
        if (videoResult.if_favorite==1){
            ((MaterialButton) view.findViewById(R.id.btn_favorite)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.kitchen_24dp_fill));
        }else {
            ((MaterialButton) view.findViewById(R.id.btn_favorite)).setIcon(AppCompatResources.getDrawable(getContext(),R.drawable.kitchen_24dp));
        }
    }
    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (danmakuPlayer!=null){
            danmakuPlayer.stop();
            danmakuPlayer.release();
            danmakuPlayer=null;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer!=null){
            mediaPlayer.pause();
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    public void initPlayer() {
        if (videoResult.video_url == null) return;

        danmakuPlayer = new DanmakuPlayer(new UnlockedDanmakuRender(),new DataSource());
        danmakuPlayer.bindView(view.findViewById(R.id.sv_danmaku));
        danmakuConfig = new DanmakuConfig();
        danmakuConfig.setAllowOverlap(true);
        mediaPlayer = new ExoPlayer.Builder(getContext()).build();
        videoSurface.setPlayer(mediaPlayer);

        // 加载媒体
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoResult.video_url));
        // 设置 Referer 头
        HttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setDefaultRequestProperties(
                                Map.of("Referer", "https://bilibili.com/")
                        ).setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Ottohub 1.0.0");
        // 使用 MediaSource 时注入 DataSource
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(httpDataSourceFactory)
                .createMediaSource(mediaItem);
        if (mediaPlayer==null) return;
        mediaPlayer.setMediaSource(mediaSource);
        mediaPlayer .setRepeatMode(Player.REPEAT_MODE_ONE); // 循环播放
        mediaPlayer.prepare();
        mediaPlayer.play(); // 自动播放
        mediaPlayer.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                AlertUtil.showError(getContext(),error.toString()).show();;
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
            }
            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);

                danmakuPlayer.seekTo(newPosition.positionMs);
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying){
                    danmakuPlayer.start(danmakuConfig);
                    danmakuPlayer.seekTo(mediaPlayer.getCurrentPosition());
                }else {
                    danmakuPlayer.pause();
                }
            }
        });
        initDanmaku();
    }

    private void initDanmaku() {
        Thread thread = new Thread(()->{
            danmakuData = MyApp.getInstance().getOttohubApi().getDanmakuApi().get_danmaku(videoResult.vid);
            ApiUtil.throwApiError(danmakuData);
            loadDanmaku();
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
        thread.start();
    }
    private void loadDanmaku() {
        for (int i = 0; i < danmakuData.data.size(); i++) {
            var data = danmakuData.data.get(i);
            var mode = 0;
            if (data.mode.equals("scroll")){
                mode=DANMAKU_MODE_ROLLING;
            }else if (data.mode.equals("top")){
                mode=DANMAKU_MODE_CENTER_TOP;
            }else if (data.mode.equals("bottom")){
                mode=DANMAKU_MODE_CENTER_BOTTOM;
            }else{
                Log.w(getClass().getName(),"Unknown danmaku type: "+data.mode);
            }
            danmakuPlayer.send(new DanmakuItemData(
                    i,
                    (long)(data.time*1000),
                    data.text,
                    mode,
                    Integer.parseInt(data.font_size.substring(0,data.font_size.length()-2)),
                    Color.parseColor(data.color),
                    0,
                    DANMAKU_STYLE_NONE,
                    0,
                    null,
                    MERGED_TYPE_NORMAL
            ));
        }
    }
}