package org.eu.hanana.reimu.ottohub_andriod.data.user;

import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserListViewModel extends ListViewModelBase<UserCard> {
    @Override
    public List<UserCard> fetchFromNetwork(ListFragmentBase videoListFragment) throws IOException {
        var frag = ((UserListFragment) videoListFragment);
        UserListResult userListResult=null;
        if (frag.type.equals(UserListFragment.TYPE_SEARCH)){
            userListResult= ApiUtil.getAppApi().getUserApi().search_user_list(frag.data,36);
            if (frag.data.toLowerCase(Locale.ROOT).startsWith("uid")) {
                userListResult.user_list.addAll(0, MyApp.getInstance().getOttohubApi().getUserApi().id_user_list(Integer.parseInt(frag.data.substring(3))).user_list);
            }
        }
        var result = new ArrayList<UserCard>();
        assert userListResult != null;
        ApiUtil.throwApiError(userListResult);
        userListResult.user_list.stream().map(userResult -> new UserCard(userResult.uid,userResult.username,userResult.avatar_url,userResult.intro).withRaw(userResult)).forEach(result::add);

        //if (result.isEmpty()){
        frag.hasMoreData=false;
        //}
        return result;
    }
}
