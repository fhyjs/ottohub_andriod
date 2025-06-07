package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import static androidx.core.content.ContextCompat.startActivity;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

public class CommentCardAdapter extends CardAdapterBase<CommentCard, CommentCardViewHolder> {
    private final String type;
    private final CommentFragmentBase frag;

    public CommentCardAdapter(List<CommentCard> videoList, String type, CommentFragmentBase commentFragmentBase) {
        super(videoList);
        this.type=type;
        this.frag=commentFragmentBase;
    }

    @Override
    public CommentCardViewHolder createViewHolder(ViewGroup parent) {
        return new CommentCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_card, parent, false));
    }

    @Override
    public void makeCardUi(CommentCardViewHolder holder, CommentCard object) {
        var ctx = holder.avatar.getContext();
        holder.username.setText(object.username);
        holder.content.setText(object.content);
        holder.info.setText(object.info);
        Glide.with(holder.avatar.getContext())
                .load(object.avatarUrl)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(holder.avatar);
        holder.userinfo.setOnClickListener(v -> {
            Intent intent = new Intent(holder.userinfo.getContext(), ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,object.commentResult.uid);
            intent.putExtras(bundle);
            startActivity(holder.userinfo.getContext(),intent,null);
        });
        holder.report.setOnClickListener(v -> {
            AlertUtil.showYesNo(ctx,ctx.getString(R.string.report),ctx.getString(R.string.issure),(dialog, which) -> {
                Thread thread = new Thread(() -> {
                    if (type.equals(TYPE_VIDEO)){
                        ApiUtil.getAppApi().getCommentApi().report_video_comment(object.cid);
                    }else {
                        ApiUtil.getAppApi().getCommentApi().report_blog_comment(object.cid);
                    }
                });
                thread.start();
            },null).show();
        });
    }
}
