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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.util.InfiniteScrollListener;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFragmentBase<T extends CardAdapterBase<E,N>,N extends RecyclerView.ViewHolder,E,M extends ListViewModelBase<E>> extends Fragment {
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
                    break;
                case ERROR:
                    adapter.hideLoading();
                    showError(resource.message);
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
    private void showError(String message) {
        error=true;
        // 检查 Fragment 是否已附加到 Activity
        if (getContext() == null || isDetached()) return;

        // 加载自定义布局
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_error, null);

        // 绑定控件
        TextView tvMessage = view.findViewById(R.id.tv_message);
        Button btnOk = view.findViewById(R.id.btn_ok);
        tvMessage.setText(message);

        // 构建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);
        builder.setCancelable(false); // 禁止点击外部关闭

        AlertDialog dialog = builder.create();

        // 设置窗口参数（可选）
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.8),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // 确认按钮点击
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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