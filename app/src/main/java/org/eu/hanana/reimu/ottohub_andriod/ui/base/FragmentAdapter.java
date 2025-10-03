package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentAdapter extends RecyclerView.Adapter<FragmentAdapter.HeaderViewHolder> {

    private final View headerView;
    private final Fragment frag;
    private final Fragment sfrag;

    public FragmentAdapter(View headerView, Fragment fragment, Fragment sourceFrag) {
        this.headerView = headerView;
        this.frag=fragment;
        this.sfrag=sourceFrag;
    }

    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HeaderViewHolder(headerView);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {
        // 一般不需要 bind，因为 headerView 本身就能直接操作
        // 例如你可以在外面 headerView.findViewById 设置点击事件
    }

    @Override
    public void onViewAttachedToWindow(@NonNull HeaderViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.itemView.post(()->{
            if (!holder.itemView.isAttachedToWindow()) return;
            frag.getChildFragmentManager().beginTransaction().replace(holder.itemView.getId(), sfrag).commit();
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}