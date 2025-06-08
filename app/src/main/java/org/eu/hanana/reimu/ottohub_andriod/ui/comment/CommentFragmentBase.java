package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.comment.CommentViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;

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
            requireActivity().removeMenuProvider(refreshMenuProvider);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_video_comment, container, false);
        if (parent!=0&&parentData!=null) {
            Toolbar toolbar  = new Toolbar(getContext());
            toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            toolbar.setBackgroundResource(com.google.android.material.R.color.design_default_color_primary);
            toolbar.setTitle(getString(R.string.reply_of)+" "+parentData.username);
            toolbar.setTitleTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_surface));
            ((ViewGroup) inflate).addView(toolbar, 0);
        }
        return inflate;
    }

    @Override
    public CommentCardAdapter createAdapter(List<CommentCard> list) {
        return new CommentCardAdapter(list,type,this);
    }
}
