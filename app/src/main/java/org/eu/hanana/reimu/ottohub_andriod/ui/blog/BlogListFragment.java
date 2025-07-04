package org.eu.hanana.reimu.ottohub_andriod.ui.blog;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_TYPE;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_BLOG;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;
import static org.eu.hanana.reimu.ottohub_andriod.ui.user.ProfileFragment.Arg_Uid;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ACTION_SEARCH;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_DATA;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.ottohub_andriod.MainActivity;
import org.eu.hanana.reimu.ottohub_andriod.R;

import org.eu.hanana.reimu.ottohub_andriod.activity.SearchActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.blog.BlogViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.ProfileFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.InfiniteScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlogListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlogListFragment extends Fragment {
    @Nullable
    public Integer uid;
    @Nullable
    public String data;
    @Nullable
    public String action=null;
    public BlogListFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BlogListFragment newInstance() {
        BlogListFragment fragment = new BlogListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public RecyclerView recyclerView;
    private BlogCardAdapter adapter;
    private final List<BlogResult> videoList = new ArrayList<>();
    public int currentPage = 0;
    public boolean hasMoreData = true;
    private BlogViewModel viewModel;
    private InfiniteScrollListener scrollListener;
    public Button selectedButton;
    public boolean error;
    public int[] buttonLabels = {R.string.recommend,R.string.latest,R.string.week_hot,R.string.monthly_hot,R.string.sesson_hot};
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(Arg_Uid)) {
            uid = getArguments().getInt(Arg_Uid);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_DATA)) {
            data = getArguments().getString(ARG_DATA);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_ACTION)) {
            action = getArguments().getString(ARG_ACTION);
        }
        viewModel = new ViewModelProvider(this).get(BlogViewModel.class);

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
    private void updateVideoList(List<BlogResult> data) {
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_blog_list, container, false);
        LinearLayout button_area = inflate.findViewById(R.id.video_type_button_area);

        for (int i = 0; i < buttonLabels.length; i++) {
            Button button = getTypeBtn(buttonLabels, i);

            button_area.addView(button);
            if (selectedButton==null){
                selectedButton=button;
            }
        }
        if (action!=null||uid!=null||data!=null||(getParentFragment()!=null&&getParentFragment().getClass()== ProfileFragment.class)){
            button_area.removeAllViews();
        }
        return inflate;
    }

    @NonNull
    private Button getTypeBtn(int[] buttonLabels, int i) {
        Button button = new MaterialButton(getContext());
        // 设置按钮样式
        button.setText(buttonLabels[i]);
        button.setTag(buttonLabels[i]);
        button.setPadding(32, 16, 32, 16);

        // 设置点击事件
        button.setOnClickListener(v -> {
            selectedButton= (Button) v;
            updateVidType();
        });

        // 添加间距
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 0, 16, 0);
        button.setLayoutParams(params);
        return button;
    }

    public void updateVidType() {
        LinearLayout btn_area = view.findViewById(R.id.video_type_button_area);
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < btn_area.getChildCount(); i++) {
            View child = btn_area.getChildAt(i);
            if (child instanceof Button) {
                buttons.add((Button) child);
            }
        }
        for (Button button : buttons) {
            if (button!=selectedButton){
                button.setEnabled(true);
            }
        }
        selectedButton.setEnabled(false);
        refresh();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setItemAnimator(null);
        // 初始化适配器
        adapter = new BlogCardAdapter(videoList);
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
        requireActivity().addMenuProvider(new BlogListFragment.MyMenuProvider(), getViewLifecycleOwner());
        updateVidType();
    }
    // 定义 MenuProvider
    private class MyMenuProvider implements MenuProvider {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            if (getActivity().getClass()!=MainActivity.class||(getParentFragment()!=null&&getParentFragment().getClass()== ProfileFragment.class))
                return;
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
            if (id == R.id.action_search_button) {
                // 创建 Intent
                Intent intent = new Intent(getActivity(), SearchActivity.class);

                // 添加额外数据（可选）
                intent.putExtra(ARG_TYPE, TYPE_BLOG);

                // 启动 Activity
                startActivity(intent); // 简单启动
                return true;
            }
            return false;
        }
    }
    public void refresh(){
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