package org.eu.hanana.reimu.ottohub_andriod.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private int visibleThreshold = 5;  // 提前加载的阈值（距离底部5项时触发）
    public boolean isLoading = false;

    public abstract void loadMoreData();  // 加载更多数据的抽象方法

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItemPosition + visibleThreshold)) {
            isLoading = true;
            loadMoreData(); // 触发加载更多
        }
    }

    public void setLoadingComplete() {
        isLoading = false;
    }
}