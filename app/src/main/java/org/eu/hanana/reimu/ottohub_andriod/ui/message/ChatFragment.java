package org.eu.hanana.reimu.ottohub_andriod.ui.message;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.FragActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.base.text.TextCard;
import org.eu.hanana.reimu.ottohub_andriod.data.message.ChatViewModule;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.BaseFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextCardAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.list.TextListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.List;


public class ChatFragment extends TextListFragmentBase<ChatViewModule> {

    public UserResult data;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public Class<ChatViewModule> getViewModelClass() {
        return ChatViewModule.class;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = new Gson().fromJson(getArguments().getString("data"),UserResult.class);
        }
        if (getActivity() instanceof FragActivity fragActivity) {
            fragActivity.toolbar.setSubtitle("uid: "+data.uid);
            var toolbar = fragActivity.toolbar;
            int sizePx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    35,
                    toolbar.getResources().getDisplayMetrics()
            );
            Glide.with(requireContext())
                    .asDrawable()
                    .load(data.avatar_url)
                    .override(sizePx)
                    .circleCrop()
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(
                                @NonNull Drawable resource,
                                @Nullable Transition<? super Drawable> transition) {

                            toolbar.setLogo(resource);
                            if (resource instanceof Animatable) {
                                ((Animatable) resource).start(); // ⚠ 只能启动一次
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            toolbar.setLogo(placeholder);
                        }
                    });
        }
    }

    @Override
    protected void registerMenuProviders() {
        super.registerMenuProviders();
        requireActivity().addMenuProvider(refreshMenuProvider=new MyMenuProvider(){
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                super.onPrepareMenu(menu);
                menu.findItem(R.id.action_search_button).setVisible(false);
            }
        }, getViewLifecycleOwner());
    }

    @Override
    public int getSpanCount() {
        return 1;
    }

    @Override
    public TextCardAdapter createAdapter(List<TextCard> list) {
        return new ChatAdapter(list,this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_send).setOnClickListener(v -> {
            var text = ((EditText) view.findViewById(R.id.et_message)).getText().toString();
            ((EditText) view.findViewById(R.id.et_message)).getText().clear();
            Thread thread = new Thread(()->{
                ApiUtil.throwApiError(ApiUtil.getAppApi().getMessageApi().send_message(data.uid,text));
                requireActivity().runOnUiThread(this::refresh);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(requireActivity()));
            thread.start();
        });
    }

    @Override
    public RecyclerView findRecyclerView(View view) {
        return view.findViewById(R.id.rv_chat);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}