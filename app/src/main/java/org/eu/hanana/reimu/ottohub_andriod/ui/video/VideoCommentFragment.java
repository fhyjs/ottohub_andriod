
package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoCommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoCommentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_VDATA = "param1";

    private VideoResult vData;

    public VideoCommentFragment() {
        // Required empty public constructor
    }


    public static VideoCommentFragment newInstance(VideoResult data) {
        VideoCommentFragment fragment = new VideoCommentFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_describe, container, false);
    }
}