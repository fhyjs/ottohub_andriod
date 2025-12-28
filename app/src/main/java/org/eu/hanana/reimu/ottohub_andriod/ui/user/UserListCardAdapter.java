package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.FragActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.CardAdapterBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.message.ChatFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.SharedPreferencesKeys;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.VibrateUtil;

import java.util.List;
import java.util.Locale;

public class UserListCardAdapter extends CardAdapterBase<UserCard, UserCardViewHolder> {
    private final UserListFragment userListFragment;

    public UserListCardAdapter(List<UserCard> videoList, UserListFragment userListFragment) {
        super(videoList);
        this.userListFragment=userListFragment;
    }

    @Override
    public UserCardViewHolder createViewHolder(ViewGroup parent) {
        return new UserCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_card, parent, false));
    }

    @Override
    public void makeCardUi(UserCardViewHolder holder, UserCard object) {
        var ctx = holder.avatar.getContext();
        holder.username.setText(object.username);
        holder.info.setText(object.info);
        Glide.with(ctx)
                .load(object.avatarUrl)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(holder.avatar);
        holder.itemView.setOnClickListener(v -> {
            if (userListFragment.type.equals(UserListFragment.TYPE_SEARCH)||userListFragment.type.equals(UserListFragment.TYPE_FOLLOWER)||userListFragment.type.equals(UserListFragment.TYPE_FOLLOWING)){
                // 创建 Intent
                Intent intent = new Intent(ctx, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(ProfileActivity.KEY_UID,object.uid);
                intent.putExtras(bundle);
                // 启动 Activity
                ctx.startActivity(intent); // 简单启动
            }else if (userListFragment.type.equals(UserListFragment.TYPE_SWITCH_ACCOUNT)){
                ApiUtil.loginWithAlert(userListFragment.getActivity(),object.uid,object.getResult().cover_url,loginResult -> {
                    if (!loginResult.isSuccess()){
                        AlertUtil.showError(ctx,loginResult.getMessage()).show();
                    }else{
                        AlertDialog alertDialog = AlertUtil.showMsg(ctx, ctx.getString(R.string.ok), ctx.getString(R.string.welcome));
                        alertDialog.setOnDismissListener(dialog -> userListFragment.getActivity().finish());
                        alertDialog.show();
                    }
                });
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (userListFragment.type.equals(UserListFragment.TYPE_SWITCH_ACCOUNT)){
                VibrateUtil.vibrate(ctx,200);
                AlertUtil.showYesNo(ctx, ctx.getString(R.string.delete), "sure?", (dialog, which) -> {
                    MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Account_List, MODE_PRIVATE).edit().remove(String.valueOf(object.uid)).apply();
                    userListFragment.refresh();
                }, null).show();
            }
            return false;
        });
        TextView badge = holder.itemView.findViewById(R.id.badge_text_view);
        badge.setVisibility(GONE);
        if (userListFragment.type.equals(UserListFragment.TYPE_SWITCH_ACCOUNT)){
           if (ApiUtil.isLogin()){
               if (Integer.parseInt(ApiUtil.getAppApi().getLoginResult().uid)==object.uid) {
                   holder.info.setText(String.format(Locale.getDefault(),"%s🌟 | %s",ctx.getString(R.string.current),object.info));
                   holder.itemView.setBackgroundTintList(ColorStateList.valueOf(ThemeUtil.getTheme(ctx).getColorPrimary()));
               }else {
                   holder.itemView.setBackgroundTintList(null);
               }
           }
        }else if (userListFragment.type.equals(UserListFragment.TYPE_CHAT_GENERAL)){
            badge.setText(String.valueOf(object.result.new_message_num));
            badge.setVisibility(object.result.new_message_num!=0?VISIBLE:GONE);
            holder.itemView.setOnClickListener(v -> {
                var arg = new Bundle();
                arg.putString("data",new Gson().toJson(object.getResult()));
                userListFragment.startActivity(FragActivity.create(userListFragment.requireContext(), ChatFragment.class,arg,userListFragment.getString(R.string.chat_with,object.username)));
            });
        }
    }
}
