package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import static android.view.View.GONE;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.video.VideoCard;

import java.util.List;

public abstract class CardAdapterBase<E,T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_END = 2;

    private List<E> videoList;
    boolean isLoading = false;
    public CardAdapterBase(List<E> videoList) {
        this.videoList = videoList;
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
    // 底部空只是
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoading;
        ProgressBar progressBar;
        public EmptyViewHolder(View itemView) {
            super(itemView);
            tvLoading = itemView.findViewById(R.id.tvLoading);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressBar.setVisibility(GONE);
            tvLoading.setText(R.string.reach_end);
        }
    }
    @Override
    public int getItemViewType(int position) {
        // 最后一个位置显示加载提示
        var b = (position == videoList.size() && isLoading) ? TYPE_LOADING : TYPE_ITEM;
        if (position==videoList.size()&&b==TYPE_ITEM){
            return TYPE_END;
        }
        return b;
    }
    public abstract T createViewHolder(ViewGroup parent);
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading_footer, parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == TYPE_END) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading_footer, parent, false);
            return new EmptyViewHolder(view);
        } else {
            return createViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 跳过加载项的处理
        if (holder instanceof LoadingViewHolder||holder instanceof EmptyViewHolder) {
            return;
        }
        @SuppressWarnings("unchecked")
        T vcvHolder = (T) holder;
        E video = videoList.get(position);
        makeCardUi(vcvHolder, video);
    }

    public abstract void makeCardUi(T holder, E object);

    @Override
    public int getItemCount() {
        // 数据项数量 + 是否显示加载项
        return videoList.size() + 1;
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