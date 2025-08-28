package org.eu.hanana.reimu.ottohub_andriod.ui.audit;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter.ARG_RESULT;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter.ARG_TARGET;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_BLOG;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_VIDEO;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ImageViewActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoDescribeFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;


public class AuditVideoFragment extends BaseFragment {

    private static final String ARG_VDATA = "param1";

    private VideoResult vData;
    private View view;

    public AuditVideoFragment() {
        // Required empty public constructor
    }

    public static AuditVideoFragment newInstance(VideoResult data) {
        AuditVideoFragment fragment = new AuditVideoFragment();
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
        this.view = view;
        view.findViewById(R.id.group_user).setVisibility(GONE);
        view.findViewById(R.id.group_audit).setVisibility(VISIBLE);
        if (vData==null){
            Toast.makeText(getContext(),R.string.error, Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
        ((TextView) view.findViewById(R.id.video_desc_text)).setText(getString(R.string.audit_video_desc,vData.title,vData.intro,vData.tag));
        UiUtil.loadImgToImageView(view.findViewById(R.id.ivThumbnail),vData.cover_url);
        view.findViewById(R.id.btn_approve).setOnClickListener(v -> {
            AlertUtil.showYesNo(getContext(),getString(R.string.approve),getString(R.string.issure),(dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_RESULT, true);
                intent.putExtra(AuditAdapter.ARG_TYPE, TYPE_VIDEO);
                intent.putExtra(ARG_TARGET, vData.vid);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            },null).show();
        });
        view.findViewById(R.id.btn_reject).setOnClickListener(v -> {
            AlertUtil.showYesNo(getContext(),getString(R.string.reject),getString(R.string.issure),(dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_RESULT, false);
                intent.putExtra(AuditAdapter.ARG_TYPE, TYPE_VIDEO);
                intent.putExtra(ARG_TARGET, vData.vid);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            },null).show();
        });
        view.findViewById(R.id.ivThumbnail).setOnClickListener(v -> {
            ImageViewActivity.start(getContext(),vData.cover_url);
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audit_video, container, false);
    }
}