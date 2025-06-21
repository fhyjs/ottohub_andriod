package org.eu.hanana.reimu.ottohub_andriod.data.user;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.lib.ottohub.api.auth.LoginResult;
import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentListResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserListResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.UserCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.UserListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClassUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserListViewModel extends ListViewModelBase<UserCard> {
    @Override
    public List<UserCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var frag = ((UserListFragment) videoListFragment);
        UserListResult userListResult=null;
        frag.hasMoreData=true;
        if (frag.type.equals(UserListFragment.TYPE_SEARCH)){
            userListResult= ApiUtil.getAppApi().getUserApi().search_user_list(frag.data,36);
            if (frag.data.toLowerCase(Locale.ROOT).startsWith("uid")) {
                userListResult.user_list.addAll(0, MyApp.getInstance().getOttohubApi().getUserApi().id_user_list(Integer.parseInt(frag.data.substring(3))).user_list);
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
}
