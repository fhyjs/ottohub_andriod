package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.LoginActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
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
    public static final String TYPE_FOLLOWING = "fi";
    public static final String TYPE_SWITCH_ACCOUNT = "swa";
    public static final String TYPE_FOLLOWER = "fe";


    // TODO: Rename and change types of parameters
    public String type;
    public String data;

    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    protected void registerMenuProviders() {
        if (type.equals(TYPE_SWITCH_ACCOUNT)){
            requireActivity().addMenuProvider(new AddMenuProvider(), getViewLifecycleOwner());
        }
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public RecyclerView findRecyclerView(View view) {
        return view.findViewById(R.id.recyclerView);
    }

    @Override
    public UserListCardAdapter createAdapter(List<UserCard> list) {
        return new UserListCardAdapter(list,this);
    }
    protected class AddMenuProvider implements MenuProvider{

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.add_menu,menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId()==R.id.action_menu_add){
                Intent intent = new Intent(getContext(), LoginActivity.class);
                // 启动 Activity
                startActivity(intent); // 简单启动
                getActivity().finish();
                return true;
            }
            return false;
        }
    }
}