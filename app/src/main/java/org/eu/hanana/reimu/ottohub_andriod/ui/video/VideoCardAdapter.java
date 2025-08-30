package org.eu.hanana.reimu.ottohub_andriod.ui.video;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.getString;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.video.VideoCard;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClipboardUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

import java.util.List;

import lombok.Setter;

// VideoCardAdapter.java
public class VideoCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;
    private static final String TAG = "VideoCardAdapter";

    private List<VideoCard> videoList;
    boolean isLoading = false;
    @Setter
    private VideoListFragment frag;

    public VideoCardAdapter(List<VideoCard> videoList) {
        this.videoList = videoList;
    }

    // ViewHolder
    public static class VideoCardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail, ivPlay,ivAvatar;
        TextView tvDuration, tvTitle, tvAuthor, tvViews;
        public VideoCardViewHolder(View itemView) {
            super(itemView);
            ThemeUtil.apply(itemView);
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
            ThemeUtil.apply(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvLoading = itemView.findViewById(R.id.tvLoading);
        }
    }
    @Override
    public int getItemViewType(int position) {
        // 最后一个位置显示加载提示
        // 如果有 footer 且到最后一个位置
        if (isLoading && position == getItemCount() - 1) {
            return TYPE_LOADING;
        }
        return TYPE_ITEM;
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
            if (position>=videoList.size()||position<0) return;
            VideoCard video = videoList.get(position);

            // 绑定数据
            // 加载到 ImageView
            Glide.with(vcvHolder.ivThumbnail.getContext())
                    .load(video.getPic_url())
                    .placeholder(R.drawable.ic_launcher_background)  // 占位图
                    .error(R.drawable.error_48px)        // 错误图
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "onImageLoadFailed: "+video.getPic_url(), e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
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

            //new
            if (frag.action.equals(VideoListFragment.ACTION_MINE)){
                vcvHolder.ivAvatar.setVisibility(GONE);
                vcvHolder.tvAuthor.setVisibility(GONE);
                vcvHolder.itemView.findViewById(R.id.group_manage).setVisibility(VISIBLE);
                Button btnAuditStatus = vcvHolder.itemView.findViewById(R.id.btn_audit_status);
                if(video.getRaw().audit_status==0){
                    btnAuditStatus.setText(R.string.under_review);
                }else if(video.getRaw().audit_status==1){
                    btnAuditStatus.setText(R.string.approved);
                }else if(video.getRaw().audit_status==2){
                    btnAuditStatus.setText(R.string.appeal);
                    btnAuditStatus.setOnClickListener(v -> {
                        var ctx = btnAuditStatus.getContext();
                        AlertUtil.showYesNo(ctx,ctx.getString( R.string.appeal), ctx.getString(R.string.appeal_content),(dialog, which) -> {
                            Thread thread = new Thread(()->{
                                ApiUtil.getAppApi().getManageApi().appeal_video(video.getVid());
                                frag.getActivity().runOnUiThread(() -> {
                                    btnAuditStatus.setText(R.string.under_review);
                                });
                            });
                            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(frag.getActivity()));
                            thread.start();
                        },null).show();
                    });
                }else {
                    btnAuditStatus.setText(R.string.under_development);
                }
            }
            vcvHolder.itemView.findViewById(R.id.btn_share).setOnClickListener(v -> {
                var txt = v.getContext().getString(R.string.share_content,video.getTitle(),"https://ottohub.cn/v/"+video.getVid());
                ClipboardUtil.copyToClipboard(v.getContext(),txt);
                shareText(v.getContext(),txt);
            });
            vcvHolder.itemView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
                AlertUtil.showYesNo(v.getContext(),v.getContext().getString(R.string.delete),v.getContext().getString(R.string.delete_msg),(dialog, which) -> {
                    Thread thread = new Thread(()->{
                        ApiUtil.getAppApi().getManageApi().delete_video(video.getVid());
                        frag.getActivity().runOnUiThread(() -> {
                            frag.refresh();
                        });
                    });
                    thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(frag.getActivity()));
                    thread.start();
                },null).show();
            });
        }
    }
    private void shareText(Context c, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain"); // 分享纯文本
        intent.putExtra(Intent.EXTRA_TEXT, text);

        // 弹出系统分享面板
        Intent chooser = Intent.createChooser(intent, "Send to...");
        c.startActivity(chooser);
    }
    @Override
    public int getItemCount() {
        // 数据项数量 + 是否显示加载项
        return videoList.size() + (isLoading ? 1 : 0);
    }

    // 控制加载提示的显示/隐藏
    public void showLoading() {
        if (!isLoading) {
            isLoading = true;
            notifyItemInserted(getItemCount() - 1); // 插入到最后一个位置
        }
    }

    public void hideLoading() {
        if (isLoading) {
            int pos = getItemCount() - 1;
            isLoading = false;
            notifyItemRemoved(pos); // 移除最后一个位置
        }
    }
}