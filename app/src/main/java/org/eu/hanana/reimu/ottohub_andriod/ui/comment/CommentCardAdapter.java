package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.startActivity;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_PARENT_DATA;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

public class CommentCardAdapter extends CardAdapterBase<CommentCard, CommentCardViewHolder> {
    private static final String TAG = "CommentCardAdapter";
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
    public void makeCardUi(final CommentCardViewHolder holder,final CommentCard object) {
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
        holder.showReply.setText(holder.itemView.getContext().getString(R.string.show_child_comment,object.commentResult.child_comment_num));
        holder.showReply.setOnClickListener(v -> {
            FragmentManager fm = frag.getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(
                    R.anim.enter_from_bottom,
                    R.anim.exit_to_bottom,
                    R.anim.pop_enter_from_bottom,
                    R.anim.pop_exit_to_bottom);
            // 当前 Fragment 隐藏
            ft.hide(frag);

            // 查找是否已存在目标 Fragment
            String tag = "comment_" + object.cid; // 用唯一 tag 标识
            Fragment target = fm.findFragmentByTag(tag);

            if (target == null) {
                // 新建并添加
                target = CommentFragmentBase.newInstance(frag.getDataId(), object.cid, frag.getType());
                target.getArguments().putString(ARG_PARENT_DATA,new Gson().toJson(object));
                ft.add(R.id.fragment_container, target, tag);
            } else {
                // 已存在，直接显示
                return;
            }

            // 加入自定义返回栈（模拟效果）
            ft.addToBackStack(null);
            ft.commit();
        });
        Log.d(TAG, "makeCardUi: "+object.username+": "+object.commentResult.child_comment_num);
        if (object.commentResult.child_comment_num != 0) {
            holder.showReply.setVisibility(View.VISIBLE);
            holder.showReply.setText(holder.itemView.getContext().getString(R.string.show_child_comment, object.commentResult.child_comment_num));
        } else {
            holder.showReply.setVisibility(View.GONE);
        }
    }
}
