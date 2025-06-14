package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.comment.IfGetExpResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.comment.CommentViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.util.List;

import lombok.Getter;

public class CommentFragmentBase extends ListFragmentBase<CommentCardAdapter, CommentCardViewHolder, CommentCard, CommentViewModel> {
    public static final String ARG_ID = "id";
    public static final String ARG_PARENT_DATA = "parent_data";
    public static final String ARG_TYPE = "type";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_BLOG = "blog";
    public static final String ARG_PARENT = "parent";

    @Getter
    protected int dataId;
    @Getter
    protected String type;
    @Getter
    protected int parent = 0;
    @Nullable
    @Getter
    protected CommentCard parentData;
    public CommentFragmentBase(){ }
    public static CommentFragmentBase newInstance(int id,int parent,String type) {
        var cfb = new CommentFragmentBase();
        var args = new Bundle();
        args.putInt(ARG_ID,id);
        args.putInt(ARG_PARENT,parent);
        args.putString(ARG_TYPE,type);
        cfb.setArguments(args);
        return cfb;
    }

    @Override
    public Class<CommentViewModel> getViewModelClass() {
        return CommentViewModel.class;
    }

    @Override
    public int getSpanCount() {
        return 1;
    }

    @Override
    public RecyclerView findRecyclerView(View view) {
        return view.findViewById(R.id.recyclerView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            type=arguments.getString(ARG_TYPE);
            dataId=arguments.getInt(ARG_ID);
            parent=arguments.getInt(ARG_PARENT);
            if (arguments.containsKey(ARG_PARENT_DATA))
                parentData=new Gson().fromJson(arguments.getString(ARG_PARENT_DATA),CommentCard.class);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                boolean canPopBackStack = getParentFragmentManager().getBackStackEntryCount() > 0;

                if (canPopBackStack) {
                    getParentFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        };

        // 添加回调到生命周期
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        if (parent!=0){
            if (refreshMenuProvider!=null)
                requireActivity().removeMenuProvider(refreshMenuProvider);
        }
    }

    @Override
    protected void registerMenuProviders() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_video_comment, container, false);
        // 1. 假设这是你的根布局，确保它是 FrameLayout 或者支持悬浮的容器
        ViewGroup rootView = (ViewGroup) inflate; // 或者你自己的容器，比如 FrameLayout
        if (parent!=0&&parentData!=null) {
            Toolbar toolbar  = new Toolbar(getContext());
            toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            toolbar.setBackgroundResource(com.google.android.material.R.color.design_default_color_primary);
            toolbar.setTitle(getString(R.string.reply_of)+" "+parentData.username);
            toolbar.setTitleTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_surface));
            // 设置返回按钮图标和颜色
            Drawable navIcon = ContextCompat.getDrawable(inflate.getContext(), androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            if (navIcon != null) {
                navIcon.setTint(ContextCompat.getColor(inflate.getContext(), R.color.white));
                toolbar.setNavigationIcon(navIcon);
            }
                        // 设置返回按钮点击事件
            toolbar.setNavigationOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
            ((LinearLayout) rootView.getChildAt(rootView.getChildCount() - 1)).addView(toolbar, 0);
        }


// 4. 创建悬浮按钮
        FloatingActionButton fab = new FloatingActionButton(getContext());
        // 设置返回按钮图标和颜色
        Drawable navIcon = ContextCompat.getDrawable(inflate.getContext(), android.R.drawable.ic_menu_edit);
        if (navIcon != null) {
            navIcon.setTint(ContextCompat.getColor(inflate.getContext(), R.color.white));
            fab.setImageDrawable(navIcon); // 替换成你的图标
        }
        fab.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getContext(), R.color.bottom_nav_color)));

// 5. 设置悬浮按钮布局参数（右下角悬浮）
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END);

        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        fabParams.setMargins(margin, margin, margin, margin);
        fab.setLayoutParams(fabParams);

// 6. 添加点击事件
        fab.setOnClickListener(v -> {
            AlertUtil.showInput(inflate.getContext(),input -> {
                Thread thread = new Thread(() -> {
                    IfGetExpResult ifGetExpResult;
                    if (getType().equals(TYPE_BLOG)){
                        ifGetExpResult = ApiUtil.getAppApi().getCommentApi().comment_blog(dataId, parent, input);
                    }else if (getType().equals(TYPE_VIDEO)){
                        ifGetExpResult = ApiUtil.getAppApi().getCommentApi().comment_video(dataId, parent, input);
                    } else {
                        ifGetExpResult = null;
                    }
                    ApiUtil.throwApiError(ifGetExpResult);
                    getActivity().runOnUiThread(()->{
                        if (ifGetExpResult.if_get_experience!=0){
                            Toast.makeText(getContext(),R.string.exp3, Toast.LENGTH_SHORT).show();
                        }
                        refresh();
                    });
                });
                thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(getActivity()));
                thread.start();
            }).show();
        });

// 7. 添加悬浮按钮到根布局
        rootView.addView(fab,0);
        return inflate;
    }

    @Override
    public CommentCardAdapter createAdapter(List<CommentCard> list) {
        return new CommentCardAdapter(list,type,this);
    }
}
