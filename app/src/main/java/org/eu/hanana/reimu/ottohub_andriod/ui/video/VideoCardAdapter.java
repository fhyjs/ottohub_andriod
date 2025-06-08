package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.video.VideoCard;

import java.util.List;

// VideoCardAdapter.java
public class VideoCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private List<VideoCard> videoList;
    boolean isLoading = false;
    public VideoCardAdapter(List<VideoCard> videoList) {
        this.videoList = videoList;
    }

    // ViewHolder
    public static class VideoCardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail, ivPlay,ivAvatar;
        TextView tvDuration, tvTitle, tvAuthor, tvViews;
        public VideoCardViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            tvDuration = itemView.findViewById(R.id.tvDuration);
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
        return (position == videoList.size() && isLoading) ? TYPE_LOADING : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading_footer, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video_card, parent, false);
            return new VideoCardViewHolder(view);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoCardViewHolder) {
            var vcvHolder = ((VideoCardViewHolder) holder);
            VideoCard video = videoList.get(position);

            // 绑定数据
            // 加载到 ImageView
            Glide.with(vcvHolder.ivThumbnail.getContext())
                    .load(video.getPic_url())
                    .placeholder(R.drawable.ic_launcher_background)  // 占位图
                    .error(R.drawable.error_48px)        // 错误图
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                    .into(vcvHolder.ivThumbnail);
            Glide.with(vcvHolder.ivAvatar.getContext())
                    .load(video.getUser_url())
                    .placeholder(R.drawable.ic_launcher_background)  // 占位图
                    .error(R.drawable.error_48px)        // 错误图
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                    .into(vcvHolder.ivAvatar);
            vcvHolder.tvDuration.setText(video.getDuration());
            vcvHolder.tvTitle.setText(video.getTitle());
            vcvHolder.tvAuthor.setText(video.getAuthor());
            vcvHolder.tvViews.setText(video.getViews());
            // 卡片点击事件
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), VideoPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(VideoPlayerActivity.KEY_VID,video.getVid());
                intent.putExtras(bundle);
                startActivity(holder.itemView.getContext(),intent,null);
            });
        }
    }
    @Override
    public int getItemCount() {
        // 数据项数量 + 是否显示加载项
        return videoList.size() + (isLoading ? 1 : 0);
    }

    // 控制加载提示的显示/隐藏
    public void showLoading() {
        isLoading = true;
        notifyItemInserted(videoList.size());
    }

    public void hideLoading() {
        isLoading = false;
        notifyItemRemoved(videoList.size());
    }
}