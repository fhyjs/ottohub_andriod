package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.startActivity;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_PARENT_DATA;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_BLOG;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;
import static org.eu.hanana.reimu.ottohub_andriod.util.UiUtil.toCssColor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.comment.IfGetExpResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CustomWebView;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

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
        //holder.content.setText(object.content);
        holder.llContent.removeAllViews();
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
                thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(frag.getActivity()));
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
        holder.reply.setOnClickListener(v -> {
            AlertUtil.showInput(ctx,input -> {
                Thread thread = new Thread(() -> {
                    IfGetExpResult ifGetExpResult;
                    if (frag.getType().equals(TYPE_BLOG)){
                        if (object.parent!=0) {
                            ifGetExpResult = ApiUtil.getAppApi().getCommentApi().comment_blog(frag.getDataId(), object.parent, "@"+object.username+" "+input);
                        }else {
                            ifGetExpResult = ApiUtil.getAppApi().getCommentApi().comment_blog(frag.getDataId(), object.cid, input);
                        }
                    }else if (frag.getType().equals(TYPE_VIDEO)){
                        if (object.parent!=0) {
                            ifGetExpResult = ApiUtil.getAppApi().getCommentApi().comment_video(frag.getDataId(), object.parent, "@"+object.username+" "+input);
                        }else {
                            ifGetExpResult = ApiUtil.getAppApi().getCommentApi().comment_video(frag.getDataId(), object.cid, input);
                        }
                    } else {
                        ifGetExpResult = null;
                    }
                    ApiUtil.throwApiError(ifGetExpResult);
                    frag.getActivity().runOnUiThread(()->{
                        if (ifGetExpResult.if_get_experience!=0){
                            Toast.makeText(ctx,R.string.exp3, Toast.LENGTH_SHORT).show();
                        }
                        frag.refresh();
                    });
                });
                thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(frag.getActivity()));
                thread.start();
            }).show();
        });
        if (object.getCommentResult().isMyComment()) {
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.delete.setVisibility(View.GONE);
        }
        holder.delete.setOnClickListener(v -> {
            AlertUtil.showYesNo(ctx,ctx.getString(R.string.delete),ctx.getString(R.string.issure),(dialog, which) -> {
                Thread thread = new Thread(() -> {
                    if (type.equals(TYPE_VIDEO)){
                        ApiUtil.getAppApi().getCommentApi().delete_video_comment(object.cid);
                    }else {
                        ApiUtil.getAppApi().getCommentApi().delete_blog_comment(object.cid);
                    }
                    frag.requireActivity().runOnUiThread(frag::refresh);
                });
                thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(frag.getActivity()));
                thread.start();
            },null).show();
        });
        if (UiUtil.containsHtml(object.content)){
            var view = getContentWv(ctx);
            String html = "<html><head><style>" +
                    "body {" +
                    "  background-color:" + toCssColor(ThemeUtil.getTheme(ctx).getColorBackground()) + ";" +
                    "  color:" + toCssColor(ThemeUtil.getTheme(ctx).getColorOnPrimary()) + ";" +
                    "  margin:0;" +
                    "  padding:0;" +
                    "}" +
                    "img {" +
                    "  max-width: 100%;" +  // 保持原始宽度
                    "  object-fit: fill;" +
                    "}" +
                    "</style>" +
                    "<script src=\"https://android_asset/web/assets/jquery-3.7.1.min.js\"></script>" +
                    "<script src=\"https://android_asset/web/comment/comment.js\"></script>" +
                    "</head><body>" +
                    object.content +
                    "</body></html>";
            view.loadDataWithBaseURL("https://m.ottohub.com/",html,"text/html","utf-8",null);
            holder.llContent.addView(view);
        }else {
            TextView contentTv = getContentTv(ctx);
            contentTv.setText(object.content);
            holder.llContent.addView(contentTv);
        }
        LinearLayout tagArea = holder.itemView.findViewById(R.id.ll_tag);
        tagArea.removeAllViews();
        for (String tag : object.getCommentResult().honour.split(",")) {
            if (tag.equals("吉吉国民")) continue;
            var btn = new MaterialButton(ctx);
            btn.setTag("themed");
            btn.setText(tag);
            tagArea.addView(btn);
        }
    }
    protected TextView getContentTv(Context context){
        TextView tvContent = new TextView(context);

// 设置 tag
        tvContent.setTag("themed");

// 设置 id（需要注意 id 必须是资源里已有的）
        tvContent.setId(R.id.tvContent);

// 设置文字
        tvContent.setText("CONTENT");
        tvContent.setTextIsSelectable(true);
// 设置文字大小（sp 单位）
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

// 设置文字颜色
        tvContent.setTextColor(ThemeUtil.getTheme(context).getColorOnPrimary());

// 设置布局参数
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tvContent.setLayoutParams(lp);
        return tvContent;
    }
    protected WebView getContentWv(Context context){
        WebView webView = new CustomWebView(context);

// 设置 tag
        webView.setTag("themed");

// 设置 id（webView id 必须是资源里已有的）
        webView.setId(R.id.tvContent);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        // ✅ 启用 JS
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayoutParams(lp);
        webView.setVerticalScrollBarEnabled(false);

        return webView;
    }
}
