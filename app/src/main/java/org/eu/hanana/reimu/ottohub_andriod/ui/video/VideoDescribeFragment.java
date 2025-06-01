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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;

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
        ((TextView) view.findViewById(R.id.video_title)).setText(vData.title);
        ((TextView) view.findViewById(R.id.video_desc_text)).setText(vData.intro);
        ((TextView) view.findViewById(R.id.tvAuthor)).setText(vData.username);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_describe, container, false);
    }
}