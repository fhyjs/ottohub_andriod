package org.eu.hanana.reimu.ottohub_andriod.data.message;

import org.eu.hanana.reimu.lib.ottohub.api.comment.CommentResult;
import org.eu.hanana.reimu.lib.ottohub.api.im.MessageResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageCard {
    public final int msg_id,sender;
    @Getter
    public final String content,receiver_name,sender_avatar_url;
    @Getter
    protected MessageResult messageResult;

    public MessageCard withRaw(MessageResult messageResult){
        this.messageResult=messageResult;
        return this;
    }
}
