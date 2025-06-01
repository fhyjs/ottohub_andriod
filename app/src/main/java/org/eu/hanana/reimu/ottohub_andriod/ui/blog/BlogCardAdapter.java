package org.eu.hanana.reimu.ottohub_andriod.ui.blog;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.video.VideoCard;

import java.util.List;

public class BlogCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private List<BlogResult> blogList;
    boolean isLoading = false;
    public BlogCardAdapter(List<BlogResult> blogList) {
        this.blogList = blogList;
    }

    // ViewHolder
    public static class BlogCardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvContent, tvTitle, tvAuthor, tvViews;
        Context context;
        public BlogCardViewHolder(View itemView) {
            super(itemView);
            context=itemView.getContext();
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvViews = itemView.findViewById(R.id.tvViews);
        }
    }
    // 底部加载的 ViewHolder
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        TextView tvLoading;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvLoading = itemView.findViewById(R.id.tvLoading);
        }
    }
    @Override
    public int getItemViewType(int position) {
        // 最后一个位置显示加载提示
        return (position == blogList.size() && isLoading) ? TYPE_LOADING : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading_footer, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_blog_card, parent, false);
            return new BlogCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BlogCardViewHolder) {
            var vcvHolder = ((BlogCardViewHolder) holder);
            BlogResult blogResult = blogList.get(position);

            // 绑定数据
            // 加载到 ImageView
            Glide.with(vcvHolder.context)
                    .load(blogResult.avatar_url)
                    .placeholder(R.drawable.ic_launcher_background)  // 占位图
                    .error(R.drawable.error_48px)        // 错误图
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                    .into(vcvHolder.ivAvatar);
            vcvHolder.tvTitle.setText(blogResult.title);
            vcvHolder.tvAuthor.setText(blogResult.username);
            vcvHolder.tvContent.setText(blogResult.content);
            vcvHolder.tvViews.setText(blogResult.time);
            // 卡片点击事件
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), BlogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(BlogActivity.KEY_BID,blogResult.bid);
                intent.putExtras(bundle);
                startActivity(holder.itemView.getContext(),intent,null);
            });
        }
    }
    @Override
    public int getItemCount() {
        // 数据项数量 + 是否显示加载项
        return blogList.size() + (isLoading ? 1 : 0);
    }

    // 控制加载提示的显示/隐藏
    public void showLoading() {
        isLoading = true;
        notifyItemInserted(blogList.size());
    }

    public void hideLoading() {
        isLoading = false;
        notifyItemRemoved(blogList.size());
    }
}