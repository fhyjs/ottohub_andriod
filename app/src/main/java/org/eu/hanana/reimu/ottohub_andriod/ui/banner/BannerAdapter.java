package org.eu.hanana.reimu.ottohub_andriod.ui.banner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.lib.ottohub.api.system.SlidesResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<SlidesResult> images;
    private BannerFragment frag;

    protected BannerAdapter(List<SlidesResult> images) {
        this.images = images;
    }

    public BannerAdapter(List<SlidesResult> slidesResult, BannerFragment bannerFragment) {
        this(slidesResult);
        this.frag=bannerFragment;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View imageView =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_banner, parent, false);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        UiUtil.loadImgToImageView(holder.imageView,images.get(position).img_url);
        holder.itemView.findViewById(R.id.bar).setMinimumHeight(frag.tabLayout.getHeight());
        ((TextView) holder.itemView.findViewById(R.id.title)).setText(images.get(position).title);
        holder.itemView.setOnClickListener(v -> {
            UiUtil.openUrl(frag.getActivity(),images.get(position).href);
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageBanner);
        }
    }
}