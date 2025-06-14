package org.eu.hanana.reimu.ottohub_andriod.ui.user;

import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentResult;
import org.eu.hanana.reimu.lib.ottohub.api.user.UserResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserCard {
    public final int uid;
    public final String username,avatarUrl,info;
    @Getter
    protected UserResult result;

    public UserCard withRaw(UserResult userResult) {
        result=userResult;
        return this;
    }
}
