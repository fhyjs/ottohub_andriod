package org.eu.hanana.reimu.ottohub_andriod.ui.comment;

import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentCard {
    public final int cid,parent;
    public final String username,avatarUrl,info,content;
    @Getter
    protected CommentResult commentResult;

    public CommentCard withRaw(CommentResult commentResult){
        this.commentResult=commentResult;
        return this;
    }
}
