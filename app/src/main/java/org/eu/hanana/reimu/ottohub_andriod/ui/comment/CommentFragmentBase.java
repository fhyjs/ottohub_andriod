package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.comment.CommentViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;

import java.util.List;

import lombok.Getter;

public class CommentFragmentBase extends ListFragmentBase<CommentCardAdapter, CommentCardViewHolder, CommentCard, CommentViewModel> {
    public static final String ARG_ID = "id";
    public static final String ARG_TYPE = "type";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_BLOG = "blog";

    @Getter
    protected int dataId;
    @Getter
    protected String type;

    public CommentFragmentBase(){ }
    public static CommentFragmentBase newInstance(int id,String type) {
        var cfb = new CommentFragmentBase();
        var args = new Bundle();
        args.putInt(ARG_ID,id);
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_comment,container,false);
    }

    @Override
    public CommentCardAdapter createAdapter(List<CommentCard> list) {
        return new CommentCardAdapter(list);
    }
}
