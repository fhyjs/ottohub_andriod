package org.eu.hanana.reimu.ottohub_andriod.ui.message;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_TYPE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.message.MessageCard;
import org.eu.hanana.reimu.ottohub_andriod.data.message.MessageViewModel;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;

import java.util.List;

import lombok.Getter;

public class MessageListFragment extends ListFragmentBase<MessageCardAdapter,MessageCardViewHolder, MessageCard, MessageViewModel> {
    public static final String ARG_TYPE = CommentFragmentBase.ARG_TYPE;
    public static final String TYPE_UNREAD = "unread";
    public static final String TYPE_READ = "read";
    public static final String TYPE_SENT = "sent";
    @Getter
    protected String type = TYPE_UNREAD;
    public static MessageListFragment newInstance(String type) {
        var cfb = new MessageListFragment();
        var args = new Bundle();
        args.putString(ARG_TYPE,type);
        cfb.setArguments(args);
        return cfb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_TYPE))
                type=arguments.getString(ARG_TYPE);
        }
    }

    @Override
    public Class<MessageViewModel> getViewModelClass() {
        return MessageViewModel.class;
    }

    @Override
    public int getSpanCount() {
        return 1;
    }

    @Override
    public RecyclerView findRecyclerView(View view) {
        return view.findViewById(R.id.recyclerView);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }

    @Override
    public MessageCardAdapter createAdapter(List<MessageCard> list) {
        return new MessageCardAdapter(list,this);
    }

    @Override
    protected void registerMenuProviders() {
        super.registerMenuProviders();
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuProvider.super.onPrepareMenu(menu);
                menu.removeItem(R.id.action_search_button);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        },getViewLifecycleOwner());
    }
}
