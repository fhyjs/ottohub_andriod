package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.InfiniteScrollListener;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFragmentBase<T extends CardAdapterBase<E,N>,N extends RecyclerView.ViewHolder,E,M extends ListViewModelBase<E>> extends BaseFragment {
    public RecyclerView recyclerView;
    protected T adapter;
    protected final List<E> videoList = new ArrayList<>();
    public int currentPage = 0;
    public boolean hasMoreData = true;
    protected M viewModel;
    protected InfiniteScrollListener scrollListener;
    public boolean error;
    protected View view;
    protected MenuProvider refreshMenuProvider;
    public ListFragmentBase() {
        // Required empty public constructor
    }
    public abstract Class<M> getViewModelClass();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(getViewModelClass());

        viewModel.getVideos().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    adapter.showLoading();
                    break;
                case SUCCESS:
                    adapter.hideLoading();
                    if (resource.data != null && !resource.data.isEmpty()) {
                        updateVideoList(resource.data);
                    }
                    scrollListener.setLoadingComplete();
                    Log.d("page", "loadNextPage: "+currentPage);
                    currentPage++;

                    if (hasMoreData) {
                        // ✅ 如果没有填满屏幕，则继续加载下一页
                        recyclerView.post(() -> {
                            boolean canScrollMore = recyclerView.canScrollVertically(1);
                            if (!canScrollMore) {
                                loadNextPage();
                            }
                        });
                    }
                    break;
                case ERROR:
                    adapter.hideLoading();
                    showError(resource.msg);
                    break;
            }
        });
    }
    private void updateVideoList(List<E> data) {
        // 更新RecyclerView的逻辑
        int oldSize = videoList.size();
        videoList.addAll(data);
        if (oldSize == 0) {
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyItemRangeInserted(oldSize, data.size());
        }
    }
    private void showError(Throwable message) {
        error=true;
        // 检查 Fragment 是否已附加到 Activity
        if (getContext() == null || isDetached()) return;
        AlertUtil.showErrorWithThrowable(getContext(), message);
        return;

    }
    public abstract int getSpanCount();
    public abstract RecyclerView findRecyclerView(View view);
    public abstract T createAdapter(List<E> list);
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        recyclerView = findRecyclerView(view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), getSpanCount()));
        recyclerView.setItemAnimator(null);
        // 初始化适配器
        adapter = createAdapter(videoList);
        recyclerView.setAdapter(adapter);


        // 添加滚动监听
        recyclerView.addOnScrollListener(scrollListener =new InfiniteScrollListener() {
            @Override
            public void loadMoreData() {
                if (hasMoreData) {
                    recyclerView.post(()->{
                       loadNextPage();
                    });
                }
            }
        });
        // 注册 MenuProvider
        registerMenuProviders();

        refresh();
    }

    protected void registerMenuProviders() {
        requireActivity().addMenuProvider(refreshMenuProvider=new MyMenuProvider(), getViewLifecycleOwner());
    }

    // 定义 MenuProvider
    private class MyMenuProvider implements MenuProvider {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            // 加载菜单布局
            menuInflater.inflate(R.menu.video_list_menu, menu);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {
            // 动态调整菜单项（替代旧的 onPrepareOptionsMenu）
            MenuItem item = menu.findItem(R.id.action_refresh_button);
            if (item != null) {
                item.setVisible(true);
                item.setEnabled(true);
            }
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            // 处理点击事件
            int id = menuItem.getItemId();
            if (id == R.id.action_refresh_button) {
                refresh();
                return true;
            }
            return false;
        }
    }
    public void refresh(){
        if(adapter==null) return;
        if (adapter.isLoading) return;
        hasMoreData=true;
        error=false;
        currentPage=0;
        videoList.clear();
        adapter.notifyDataSetChanged();
        loadInitialData();
    }
    private void loadInitialData() {
        // 模拟加载第一页数据
        //List<VideoCard> newData = generateDummyData(currentPage);
        //videoList.addAll(newData);
        //adapter.notifyItemInserted(videoList.size()-newData.size());
        adapter.showLoading();
        viewModel.loadVideos(this);
    }

    private void loadNextPage() {
        if (adapter.isLoading) return;
        //adapter.setLoading(true); // 显示加载进度条
        adapter.showLoading();
        viewModel.loadVideos(this);
    }

}