
package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import android.health.connect.changelog.ChangeLogsRequest;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentListResult;
import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CustomWebView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoCommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoCommentFragment extends Fragment {
    public static final String TYPE_VIDEO = "video";
    protected String webPage = CustomWebView.internal+ "web/comment/comment.html";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_VDATA = "param1";
    private static final String ARG_Type = "type";
    protected boolean inited = false,finished= false;

    public int dataId;
    public List<CommentResult> commentResults=new ArrayList<>();
    private CustomWebView wvContent;
    public String type;

    public VideoCommentFragment() {
        // Required empty public constructor
    }


    public static VideoCommentFragment newInstance(int dataId,String type) {
        VideoCommentFragment fragment = new VideoCommentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_VDATA, dataId);
        args.putString(ARG_Type, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataId = getArguments().getInt(ARG_VDATA);
            type = getArguments().getString(ARG_Type);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_video_comment, container, false);
        wvContent=inflate.findViewById(R.id.wvContent);
        wvContent.addJavascriptInterface(new JsObj(),"comment");
        wvContent.loadUrl(webPage);
        return inflate;
    }
    private void init() {
        Thread thread = new Thread(() -> {
            finished=true;
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
        thread.start();
    }
    @Override
    public void onDestroy() {
        wvContent.destroy();
        super.onDestroy();
    }
    public class JsObj{
        @JavascriptInterface
        public String getType(){
            return type;
        }
        @JavascriptInterface
        public boolean init(){
            if (!inited) {
                inited=true;
                VideoCommentFragment.this.init();
            }
            return finished;
        }
        @JavascriptInterface
        public String getVideoCommentList(){
            return new Gson().toJson(commentResults);
        }
        @JavascriptInterface
        public String getVUrl(){
            return String.format(Locale.getDefault(),"https://m.ottohub.cn/%s/%d",type.equals(TYPE_VIDEO)?"v":"b",dataId);
        }
    }
}