package org.eu.hanana.reimu.ottohub_andriod.ui.audit;


import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_AUDIT;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_AVATAR;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_BLOG;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_COMMENT;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_COVER;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_DANMAKU;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_VIDEO;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentResult;
import org.eu.hanana.reimu.lib.ottohub.api.danmaku.DanmakuResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ImageAuditActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardViewHolder;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

public class AuditAdapter extends TextCardAdapter {
    public static final String ARG_TYPE = "type";
    public static final String ARG_RESULT = "result";
    public static final String ARG_TARGET = "target";
    private final ActivityResultLauncher<Intent> launcher;
    public AuditAdapter(List<TextCard> messageList, AuditFragment textListFragmentBase) {
        super(messageList, textListFragmentBase);

        // 注册回调
        launcher = getFrag().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            performAction(data.getBooleanExtra(ARG_RESULT, false),data.getIntExtra(ARG_TARGET,0),data.getStringExtra(ARG_TYPE));
                        }
                    }
                }
        );
    }

    private void performAction(boolean pass, int id, String type) {
        Thread thread = new Thread(() -> {
            if (type.equals(TYPE_BLOG)){
                if (pass){
                    ApiUtil.getAppApi().getModerationApi().approve_blog(id);
                }else {
                    ApiUtil.getAppApi().getModerationApi().reject_blog(id);
                }
            }else if (type.equals(TYPE_VIDEO)){
                if (pass){
                    ApiUtil.getAppApi().getModerationApi().approve_video(id);
                }else {
                    ApiUtil.getAppApi().getModerationApi().reject_video(id);
                }
            }else if (type.equals(TYPE_AVATAR)){
                if (pass){
                    ApiUtil.getAppApi().getProfileApi().approve_avatar(id);
                }else {
                    ApiUtil.getAppApi().getProfileApi().reject_avatar(id);
                }
            }else if (type.equals(TYPE_COVER)){
                if (pass){
                    ApiUtil.getAppApi().getProfileApi().approve_cover(id);
                }else {
                    ApiUtil.getAppApi().getProfileApi().reject_cover(id);
                }
            }else if (type.equals(TYPE_COMMENT+"b")){
                if (pass){
                    ApiUtil.getAppApi().getCommentApi().approve_blog_comment(id);
                }else {
                    ApiUtil.getAppApi().getCommentApi().reject_blog_comment(id);
                }
            }else if (type.equals(TYPE_COMMENT+"v")){
                if (pass){
                    ApiUtil.getAppApi().getCommentApi().approve_video_comment(id);
                }else {
                    ApiUtil.getAppApi().getCommentApi().reject_video_comment(id);
                }
            }else if (type.equals(TYPE_DANMAKU)){
                if (pass){
                    ApiUtil.getAppApi().getDanmakuApi().approve_danmaku(id);
                }else {
                    ApiUtil.getAppApi().getDanmakuApi().reject_danmaku(id);
                }
            }
            AuditAdapter.this.frag.getActivity().runOnUiThread(frag::refresh);
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getFrag().getActivity()));
        thread.start();
    }

    protected AuditFragment getFrag(){
        return (AuditFragment) frag;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void makeCardUi(TextCardViewHolder holder, TextCard object) {
        super.makeCardUi(holder, object);
        var ctx = holder.content.getContext();
        if (getFrag().type.equals(TYPE_BLOG)){
            var extra = ((BlogResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, BlogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(BlogActivity.KEY_BID,extra.bid);
                bundle.putString(BlogActivity.KEY_DATA,new Gson().toJson(extra));
                bundle.putString(BlogActivity.KEY_TYPE,TYPE_AUDIT);
                intent.putExtras(bundle);
                launcher.launch(intent);
            });
        }else if (getFrag().type.equals(TYPE_VIDEO)){
            var extra = ((VideoResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, VideoPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(VideoPlayerActivity.KEY_VID,extra.vid);
                bundle.putString(VideoPlayerActivity.KEY_DATA,new Gson().toJson(extra));
                bundle.putString(VideoPlayerActivity.KEY_TYPE,TYPE_AUDIT);
                intent.putExtras(bundle);
                launcher.launch(intent);
            });
        }else if (getFrag().type.equals(TYPE_AVATAR)){
            var extra = ((UserResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, ImageAuditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ImageAuditActivity.KEY_URL, extra.avatar_url);
                bundle.putString(ImageAuditActivity.KEY_DATA, String.valueOf(extra.uid));
                bundle.putString(ImageAuditActivity.KEY_TYPE,TYPE_AVATAR);
                intent.putExtras(bundle);
                launcher.launch(intent);
            });
        }else if (getFrag().type.equals(TYPE_COVER)){
            var extra = ((UserResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, ImageAuditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ImageAuditActivity.KEY_URL, extra.cover_url);
                bundle.putString(ImageAuditActivity.KEY_DATA, String.valueOf(extra.uid));
                bundle.putString(ImageAuditActivity.KEY_TYPE,TYPE_COVER);
                intent.putExtras(bundle);
                launcher.launch(intent);
            });
        }else if (getFrag().type.equals(TYPE_COMMENT)){
            var extra = ((CommentResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                AlertUtil.showYesNo(getFrag().getActivity(), getFrag().getString(R.string.audit_comment_confirm),extra.content, (d,b) -> {
                    performAction(true,extra.getCid(),TYPE_COMMENT+(extra.bcid>extra.vcid?"b":"v"));
                },(d,b)->{
                    performAction(false,extra.getCid(),TYPE_COMMENT+(extra.bcid>extra.vcid?"b":"v"));
                }).show();
            });
        }else if (getFrag().type.equals(TYPE_DANMAKU)){
            var extra = ((DanmakuResult) object.extra);
            holder.itemView.setOnClickListener(v -> {
                AlertUtil.showYesNo(getFrag().getActivity(), getFrag().getString(R.string.audit_comment_confirm),extra.text, (d,b) -> {
                    performAction(true, (int) extra.danmaku_id,TYPE_DANMAKU);
                },(d,b)->{
                    performAction(false, (int) extra.danmaku_id,TYPE_DANMAKU);
                }).show();
            });
        }
    }
}
