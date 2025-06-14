package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.user.UserListViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;

import java.util.List;


public class UserListFragment extends ListFragmentBase<UserListCardAdapter,UserCardViewHolder,UserCard, UserListViewModel> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_ACTION = VideoListFragment.ARG_ACTION;
    public static final String ARG_DATA = VideoListFragment.ARG_DATA;
    public static final String TYPE_SEARCH = "search";


    // TODO: Rename and change types of parameters
    public String type;
    public String data;

    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public Class<UserListViewModel> getViewModelClass() {
        return UserListViewModel.class;
    }

    public static UserListFragment newInstance(String type, String data) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTION, type);
        args.putString(ARG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_ACTION);
            data = getArguments().getString(ARG_DATA);
        }
    }

    @Override
    public int getSpanCount() {
        return 1;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_video_comment, container, false);
        return inflate;
    }

    @Override
    public RecyclerView findRecyclerView(View view) {
        return view.findViewById(R.id.recyclerView);
    }

    @Override
    public UserListCardAdapter createAdapter(List<UserCard> list) {
        return new UserListCardAdapter(list,this);
    }
}