package org.eu.hanana.reimu.ottohub_andriod.data.user;

import androidx.annotation.Nullable;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.auth.LoginResult;
import org.eu.hanana.reimu.lib.ottohub.api.im.MessageResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserListResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.MyAppApplicationLike;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.UserCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.UserListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.DataUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserListViewModel extends ListViewModelBase<UserCard> {
    public final List<UserResult> bufferedResult = new ArrayList<>();
    @Override
    public List<UserCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var frag = ((UserListFragment) videoListFragment);
        UserListResult userListResult=null;
        frag.hasMoreData=true;
        if (frag.type.equals(UserListFragment.TYPE_SEARCH)){
            userListResult= ApiUtil.getAppApi().getUserApi().search_user_list(frag.data,36);
            if (frag.data.toLowerCase(Locale.ROOT).startsWith("uid")) {
                try {
                    userListResult.user_list.addAll(0, MyAppApplicationLike.getInstance().getOttohubApi().getUserApi().id_user_list(Integer.parseInt(frag.data.substring(3))).user_list);
                }catch (Exception ignored){}
            }
            frag.hasMoreData=false;
        } else if (frag.type.equals(UserListFragment.TYPE_SWITCH_ACCOUNT)) {
            userListResult=new UserListResult();
            userListResult.status= ApiResultBase.SUCCESS;
            Map<String, LoginResult> accounts = ApiUtil.getAccounts();
            if (frag.currentPage<accounts.size()) {
                userListResult.user_list = List.of(accounts.values().stream().toList().get(frag.currentPage)).stream().map(loginResult -> {
                    var res = ApiUtil.getAppApi().getUserApi().id_user_list(Integer.parseInt(loginResult.uid)).user_list.get(0);
                    res.cover_url = loginResult.token;
                    return res;
                }).toList();
                frag.hasMoreData = true;

            }else {
                frag.hasMoreData=false;
                userListResult.user_list = List.of();
            }
        } else if (frag.type.equals(UserListFragment.TYPE_FOLLOWING)) {
            userListResult= ApiUtil.getAppApi().getFollowingApi().following_list(Integer.parseInt(frag.data),frag.currentPage*12,12);
            if (userListResult.user_list!=null&&userListResult.user_list.isEmpty()) frag.hasMoreData = false;
        } else if (frag.type.equals(UserListFragment.TYPE_FOLLOWER)) {
            userListResult= ApiUtil.getAppApi().getFollowingApi().fan_list(Integer.parseInt(frag.data),frag.currentPage*12,12);
            if (userListResult.user_list!=null&&userListResult.user_list.isEmpty()) frag.hasMoreData = false;
        } else if (frag.type.equals(UserListFragment.TYPE_CHAT_GENERAL)) {
            var friend_list= ApiUtil.getAppApi().getMessageApi().friend_list(frag.currentPage*12,12,true);
            var new_msg= ApiUtil.getAppApi().getMessageApi().unread_message_list(frag.currentPage*12,12);
            ApiUtil.throwApiError(new_msg);
            ApiUtil.throwApiError(friend_list);
            for (UserResult userResult : friend_list.user_list) {
                UserResult inBuffer = findInBuffer(userResult.uid);
                if(inBuffer !=null){
                    userResult.sex="is_friend";
                    inBuffer.new_message_num=userResult.new_message_num;
                }else {
                    userResult.sex="is_friend";
                    bufferedResult.add(userResult);
                }
            }
            for (MessageResult messageResult : new_msg.message_list) {
                UserResult inBuffer = findInBuffer(messageResult.sender);
                if(inBuffer !=null){
                    if (!inBuffer.sex.equals("is_friend")) {
                        inBuffer.new_message_num++;
                    }
                }else {
                    var usr = new UserResult();
                    usr.uid=messageResult.sender;
                    usr.avatar_url=messageResult.sender_avatar_url;
                    usr.new_message_num=1;
                    usr.sex="not_friend";
                    usr.username=messageResult.sender_name;
                    bufferedResult.add(usr);
                }
            }

            var result = DataUtil.subListSafe(bufferedResult,frag.currentPage*12,12);
            userListResult=new UserListResult();
            userListResult.user_list=result;
            userListResult.status=ApiResultBase.SUCCESS;
            if (userListResult.user_list.isEmpty()) frag.hasMoreData = false;
        }
        var result = new ArrayList<UserCard>();
        if (userListResult==null){
            userListResult=new UserListResult();
            userListResult.user_list=List.of();
            frag.hasMoreData=false;
        }
        ApiUtil.throwApiError(userListResult);
        userListResult.user_list.stream().map(userResult -> new UserCard(userResult.uid,userResult.username,userResult.avatar_url,userResult.intro).withRaw(userResult)).forEach(result::add);

        return result;
    }
    @Nullable
    private UserResult findInBuffer(int uid){
        for (UserResult userResult : bufferedResult) {
            if (userResult.uid==uid) return userResult;
        }
        return null;
    }

}
