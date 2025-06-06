package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.video.VideoCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;

import java.util.List;

public class CommentCardAdapter extends CardAdapterBase<CommentCard, CommentCardViewHolder> {
    public CommentCardAdapter(List<CommentCard> videoList) {
        super(videoList);
    }

    @Override
    public CommentCardViewHolder createViewHolder(ViewGroup parent) {
        return new CommentCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_card, parent, false));
    }

    @Override
    public void makeCardUi(CommentCardViewHolder holder, CommentCard object) {
        holder.username.setText(object.username);
        holder.content.setText(object.content);
        holder.info.setText(object.info);
        Glide.with(holder.avatar.getContext())
                .load(object.avatarUrl)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(holder.avatar);

    }
}
