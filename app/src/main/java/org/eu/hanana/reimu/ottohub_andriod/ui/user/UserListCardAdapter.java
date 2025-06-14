package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity.KEY_UID;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_TYPE;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.SearchActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentCardViewHolder;

import java.util.List;

public class UserListCardAdapter extends CardAdapterBase<UserCard, UserCardViewHolder> {
    private final UserListFragment userListFragment;

    public UserListCardAdapter(List<UserCard> videoList, UserListFragment userListFragment) {
        super(videoList);
        this.userListFragment=userListFragment;
    }

    @Override
    public UserCardViewHolder createViewHolder(ViewGroup parent) {
        return new UserCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_card, parent, false));
    }

    @Override
    public void makeCardUi(UserCardViewHolder holder, UserCard object) {
        var ctx = holder.avatar.getContext();
        holder.username.setText(object.username);
        holder.info.setText(object.info);
        Glide.with(holder.avatar.getContext())
                .load(object.avatarUrl)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(holder.avatar);
        holder.itemView.setOnClickListener(v -> {
            // 创建 Intent
            Intent intent = new Intent(ctx, ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,object.uid);
            intent.putExtras(bundle);
            // 启动 Activity
            ctx.startActivity(intent); // 简单启动
        });
    }
}
